package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.Spread
import com.quarkdown.processor.model.FunctionDescriptor
import com.quarkdown.processor.model.ModuleDescriptor
import com.quarkdown.processor.model.ParameterDescriptor
import com.quarkdown.processor.util.ModuleNaming
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
        val parameters = function.parameters.map { describe(it, ctx) ?: return null }

        return FunctionDescriptor(
            originalName = originalName,
            exportedName = exportedName,
            qualifiedName = function.qualifiedName?.asString() ?: originalName,
            returnType = returnType,
            parameters = parameters,
            declaration = function,
            sourceAnnotations = AnnotationExtractor.ForFunction.extract(function, ctx),
            kdoc = KDocExtractor.extract(function, ctx),
        )
    }

    private fun describe(
        parameter: KSValueParameter,
        ctx: DiscoveryContext,
    ): ParameterDescriptor? {
        val originalName =
            parameter.name?.asString() ?: run {
                ctx.logger.error("Unnamed parameter in @QFunction is not supported.", parameter)
                return null
            }
        if (parameter.hasAnnotation<Spread>()) {
            return describeSpread(originalName, parameter, ctx)
        }
        return ParameterDescriptor.Plain(
            originalName = originalName,
            exportedName = ctx.mappings.exportedName(parameter) ?: originalName,
            type = parameter.type.resolve(),
            defaultExpression = DefaultValueExtractor.extract(parameter, ctx),
            sourceAnnotations = AnnotationExtractor.ForParameter.extract(parameter, ctx),
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
    ): ParameterDescriptor.Spread {
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
        val components = primary.parameters.map { describePlainComponent(it, ctx) }

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
    ): ParameterDescriptor.Plain {
        val original = component.name?.asString() ?: error("Unnamed @Spread component parameter is not supported")
        return ParameterDescriptor.Plain(
            originalName = original,
            exportedName = ctx.mappings.exportedName(component) ?: original,
            type = component.type.resolve(),
            defaultExpression = DefaultValueExtractor.extract(component, ctx),
            sourceAnnotations = AnnotationExtractor.ForParameter.extract(component, ctx),
        )
    }
}
