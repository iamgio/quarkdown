package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.model.FunctionDescriptor
import com.quarkdown.processor.model.ModuleDescriptor
import com.quarkdown.processor.model.ParameterDescriptor
import com.quarkdown.processor.util.hasAnnotation
import com.quarkdown.processor.util.quarkdownName

/**
 * Pure transformation from KSP symbols to the descriptor types consumed by the code generator.
 *
 * Splitting this stage out of [ModuleDiscovery] keeps the orchestrator small
 * (scan -> validate -> describe) and lets us unit-test the KSP-to-descriptor mapping in isolation.
 *
 * `@Name` resolution flows through [NameMappings]: every declaration this stage visits has its
 * exported name recorded there before descriptors are built, so parameters can read their own
 * exported name and defaults can look up the function's rename map from the same registry
 * downstream consumers will use.
 */
internal object ModuleDescriber {
    /**
     * Builds a [ModuleDescriptor] from a `@file:QModule` source by collecting its `@QFunction`
     * declarations. The module name is the file name without the `.kt` extension.
     */
    fun describe(
        file: KSFile,
        mappings: NameMappings,
    ): ModuleDescriptor {
        val functions =
            file.declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation<QFunction>() }
                .map { describe(it, mappings) }
                .toList()

        return ModuleDescriptor(
            name = file.fileName.removeSuffix(".kt"),
            packageName = file.packageName.asString(),
            file = file,
            functions = functions,
            sourceImports = ImportExtractor.extract(file),
        )
    }

    private fun describe(
        function: KSFunctionDeclaration,
        mappings: NameMappings,
    ): FunctionDescriptor {
        val originalName = function.simpleName.asString()
        val exportedName = function.quarkdownName() ?: originalName
        mappings.record(function, exportedName)
        // Record every parameter's export up-front so the rename map is complete before any
        // default expression on this function is extracted.
        function.parameters.forEach { param ->
            val paramOriginal = param.name?.asString() ?: return@forEach
            mappings.record(param, param.quarkdownName() ?: paramOriginal)
        }

        val renames = mappings.parameterRenames(function)
        return FunctionDescriptor(
            originalName = originalName,
            exportedName = exportedName,
            qualifiedName = function.qualifiedName?.asString() ?: originalName,
            returnType = function.returnType?.resolve() ?: error("Cannot resolve return type of '$originalName'"),
            parameters = function.parameters.map { describe(it, mappings, renames) },
            declaration = function,
            sourceAnnotations = AnnotationExtractor.forFunction(function),
        )
    }

    private fun describe(
        parameter: KSValueParameter,
        mappings: NameMappings,
        renames: Map<String, String>,
    ): ParameterDescriptor {
        val originalName = parameter.name?.asString() ?: error("Unnamed parameter in @QFunction is not supported")
        return ParameterDescriptor(
            originalName = originalName,
            exportedName = mappings.exportedName(parameter) ?: originalName,
            type = parameter.type.resolve(),
            defaultExpression = DefaultValueExtractor.extract(parameter, renames),
            sourceAnnotations = AnnotationExtractor.forParameter(parameter),
        )
    }
}
