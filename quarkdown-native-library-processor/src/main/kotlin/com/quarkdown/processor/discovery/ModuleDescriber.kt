package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.annotation.QFunction
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
        val parameters = function.parameters.map { describe(it, ctx) ?: return null }

        return FunctionDescriptor(
            originalName = originalName,
            exportedName = exportedName,
            qualifiedName = function.qualifiedName?.asString() ?: originalName,
            returnType = returnType,
            parameters = parameters,
            declaration = function,
            sourceAnnotations = AnnotationExtractor.ForFunction.extract(function, ctx),
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
        val exportedName = parameter.quarkdownName() ?: originalName
        ctx.mappings.record(parameter, exportedName)
        return ParameterDescriptor(
            originalName = originalName,
            exportedName = exportedName,
            type = parameter.type.resolve(),
            defaultExpression = DefaultValueExtractor.extract(parameter, ctx),
            sourceAnnotations = AnnotationExtractor.ForParameter.extract(parameter, ctx),
        )
    }
}
