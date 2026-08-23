package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.Spread
import com.quarkdown.processor.coercion.CoercionPlan
import com.quarkdown.processor.coercion.CoercionPlanner
import com.quarkdown.processor.coercion.toTypeShape
import com.quarkdown.processor.model.FunctionDescriptor
import com.quarkdown.processor.model.ModuleDescriptor
import com.quarkdown.processor.model.ParameterDescriptor
import com.quarkdown.processor.util.ModuleNaming
import com.quarkdown.processor.util.getAnnotation
import com.quarkdown.processor.util.hasAnnotation
import com.quarkdown.processor.util.quarkdownName

/**
 * Pure transformation from KSP symbols to the descriptor types consumed by the code generator.
 *
 * Splitting this stage out of [ModuleDiscovery] keeps the orchestrator small
 * (scan -> validate -> describe) and lets us unit-test the KSP-to-descriptor mapping in isolation.
 *
 * All round-scoped state (the `NameMappings` registry, the reflective PSI facade, the logger)
 * reaches this stage through the [DiscoveryContext] parameter, so the extractors it delegates to
 * see the exact same view of the round.
 */
internal object ModuleDescriber {
    /**
     * Builds a [ModuleDescriptor] from a `@file:QModule` source by collecting its `@QFunction`
     * declarations. The module name is the file name without the `.kt` extension.
     */
    fun describe(
        file: KSFile,
        ctx: DiscoveryContext,
    ): ModuleDescriptor {
        val functions =
            file.declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation<QFunction>() }
                .mapNotNull { describe(it, ctx) }
                .toList()

        return ModuleDescriptor(
            name = ModuleNaming.moduleNameOf(file.fileName),
            packageName = file.packageName.asString(),
            file = file,
            functions = functions,
            sourceImports = ImportExtractor.extract(file, ctx),
        )
    }

    /**
     * Returns `null` for functions that fail the precondition checks already reported by
     * [ModuleValidator]. This keeps the describer from throwing when validation errors have
     * been logged but the KSP round is still running.
     */
    private fun describe(
        function: KSFunctionDeclaration,
        ctx: DiscoveryContext,
    ): FunctionDescriptor? {
        val originalName = function.simpleName.asString()
        val exportedName = function.quarkdownName() ?: originalName
        ctx.mappings.record(function, exportedName)

        val returnType =
            function.returnType?.resolve() ?: run {
                ctx.logger.error("Cannot resolve return type of '$originalName'.", function)
                return null
            }

        // Record every parameter's export up-front so the rename map is complete before any
        // default expression on this function is extracted.
        function.parameters.forEach { param ->
            val paramOriginal = param.name?.asString() ?: return@forEach
            ctx.mappings.record(param, param.quarkdownName() ?: paramOriginal)
        }
        val counter = IndexCounter()
        val parameters = function.parameters.map { describe(it, ctx, counter) ?: return null }

        return FunctionDescriptor(
            originalName = originalName,
            exportedName = exportedName,
            qualifiedName = function.qualifiedName?.asString() ?: originalName,
            returnType = returnType,
            parameters = parameters,
            declaration = function,
            sourceAnnotations = AnnotationExtractor.ForFunction.extract(function, ctx),
            kdoc = KDocExtractor.extract(function, ctx),
            validatorExpressions = validatorExpressionsOf(function),
        )
    }

    private fun describe(
        parameter: KSValueParameter,
        ctx: DiscoveryContext,
        counter: IndexCounter,
    ): ParameterDescriptor? {
        val originalName =
            parameter.name?.asString() ?: run {
                ctx.logger.error("Unnamed parameter in @QFunction is not supported.", parameter)
                return null
            }
        if (parameter.hasAnnotation<Spread>()) {
            return describeSpread(originalName, parameter, ctx, counter)
        }
        return describePlain(
            parameter = parameter,
            originalName = originalName,
            ctx = ctx,
            counter = counter,
        )
    }

    /**
     * Describes a single parameter, planning how the generated body will fill it.
     *
     * Returns `null` after reporting an error when no plan exists, which fails the KSP round
     * rather than letting the failure surface while a document is being rendered.
     */
    private fun describePlain(
        parameter: KSValueParameter,
        originalName: String,
        ctx: DiscoveryContext,
        counter: IndexCounter,
    ): ParameterDescriptor.Plain? {
        val type = parameter.type.resolve()
        val shape = type.toTypeShape()
        val isInjected = parameter.getAnnotation(INJECTED_FQN) != null
        val plan = CoercionPlanner.plan(shape, isInjected, ctx.factories)
        if (plan is CoercionPlan.Unsupported) {
            ctx.logger.error("Parameter '$originalName': ${plan.reason}.", parameter)
            return null
        }

        return ParameterDescriptor.Plain(
            originalName = originalName,
            exportedName = ctx.mappings.exportedName(parameter) ?: originalName,
            type = type,
            defaultExpression = DefaultValueExtractor.extract(parameter, ctx),
            sourceAnnotations = AnnotationExtractor.ForParameter.extract(parameter, ctx),
            index = counter.next++,
            isBody = parameter.getAnnotation(BODY_FQN) != null,
            isInjected = isInjected,
            isNullable = shape.isNullable,
            plan = plan,
        )
    }

    /**
     * Expands a `@Spread` parameter into one [ParameterDescriptor.Plain] per member of its
     * class's primary constructor.
     *
     * Records every component's exported name into the shared [NameMappings] before describing
     * any of them, so a component default that references a sibling under its exported name
     * (see [DefaultValueExtractor]) resolves against a complete rename map rather than a
     * partially-built one.
     */
    private fun describeSpread(
        outerName: String,
        parameter: KSValueParameter,
        ctx: DiscoveryContext,
        counter: IndexCounter,
    ): ParameterDescriptor.Spread? {
        val classDeclaration =
            parameter.type.resolve().declaration as? KSClassDeclaration
                ?: error("@Spread parameter '$outerName' must reference a class type")
        val primary =
            classDeclaration.primaryConstructor
                ?: error(
                    "@Spread class '${classDeclaration.qualifiedName?.asString()}' must have a primary constructor",
                )

        // Two-pass registration: components may reference each other in default expressions,
        // and the rename map must be complete before any component is described.
        primary.parameters.forEach { component ->
            val componentOriginal = component.name?.asString() ?: return@forEach
            ctx.mappings.record(component, component.quarkdownName() ?: componentOriginal)
        }
        val components = primary.parameters.map { describePlainComponent(it, ctx, counter) ?: return null }

        return ParameterDescriptor.Spread(
            originalName = outerName,
            dataClassFqn =
                classDeclaration.qualifiedName?.asString()
                    ?: error("@Spread parameter '$outerName' references an unresolvable class type"),
            components = components,
            sourceAnnotations = AnnotationExtractor.ForParameter.extract(parameter, ctx),
            dataClassKdoc = KDocExtractor.extract(classDeclaration, ctx),
        )
    }

    /**
     * Describes a single primary-constructor parameter of a spread class as a wrapper-level
     * [ParameterDescriptor.Plain]. Uses the same extractors as top-level parameters, so
     * `@Name`, defaults, and propagated annotations follow the same rules.
     */
    private fun describePlainComponent(
        component: KSValueParameter,
        ctx: DiscoveryContext,
        counter: IndexCounter,
    ): ParameterDescriptor.Plain? {
        val original = component.name?.asString() ?: error("Unnamed @Spread component parameter is not supported")
        return describePlain(parameter = component, originalName = original, ctx = ctx, counter = counter)
    }

    /**
     * Renders the document-type restrictions of [function] as constructor calls, so the generated
     * module builds its validators without reading annotations at runtime.
     */
    private fun validatorExpressionsOf(function: KSFunctionDeclaration): List<String> =
        buildList {
            entriesOf(function, ONLY_FOR_DOCUMENT_TYPE_FQN)?.let { types ->
                add("$VALIDATOR_FQN(setOf(${types.joinToString()}))")
            }
            entriesOf(function, NOT_FOR_DOCUMENT_TYPE_FQN)?.let { types ->
                add("$VALIDATOR_FQN($DOCUMENT_TYPE_FQN.entries - setOf(${types.joinToString()}))")
            }
        }

    /**
     * Fully qualified names of the enum entries passed to the `types` vararg of the annotation
     * named [annotationFqn], or `null` when the annotation is absent.
     */
    private fun entriesOf(
        function: KSFunctionDeclaration,
        annotationFqn: String,
    ): List<String>? {
        val annotation = function.getAnnotation(annotationFqn) ?: return null
        val raw = annotation.arguments.firstOrNull()?.value ?: return null
        val values = raw as? List<*> ?: listOf(raw)
        return values.mapNotNull(::enumEntryName).takeIf { it.isNotEmpty() }
    }

    /**
     * Fully qualified name of a single enum entry used as an annotation argument.
     *
     * KSP models such an argument either as a [KSType] or, on the analysis-API backend, as the
     * entry's own declaration, so both shapes are accepted.
     */
    private fun enumEntryName(entry: Any?): String? =
        when (entry) {
            is KSType -> entry.declaration.qualifiedName?.asString()
            is KSDeclaration -> entry.qualifiedName?.asString()
            else -> null
        }

    /** Running position within a function's flattened parameter list. */
    private class IndexCounter {
        var next: Int = 0
    }

    private const val BODY_FQN = "com.quarkdown.core.function.reflect.annotation.Body"
    private const val INJECTED_FQN = "com.quarkdown.core.function.reflect.annotation.Injected"
    private const val ONLY_FOR_DOCUMENT_TYPE_FQN = "com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType"
    private const val NOT_FOR_DOCUMENT_TYPE_FQN = "com.quarkdown.core.function.reflect.annotation.NotForDocumentType"
    private const val DOCUMENT_TYPE_FQN = "com.quarkdown.core.document.DocumentType"
    private const val VALIDATOR_FQN = "com.quarkdown.core.function.call.validate.DocumentTypeFunctionCallValidator"
}
