package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.model.FunctionDescriptor
import com.quarkdown.processor.model.ModuleDescriptor
import com.quarkdown.processor.model.ParameterDescriptor
import com.quarkdown.processor.util.hasAnnotation

/**
 * Pure transformation from KSP symbols to the descriptor types consumed by the code generator.
 *
 * Held as a stateless `object` because every input is a KSP node and every output is a data class:
 * there is no per-round or per-file state to manage. Splitting this stage out of [ModuleDiscovery]
 * keeps the orchestrator small (scan -> validate -> describe) and lets us unit-test the
 * KSP-to-descriptor mapping in isolation if needed.
 *
 * `@Name` is resolved here so the generator's output is mechanical: every descriptor already
 * carries the exported name to render, the original name to delegate to, and the resolved type
 * to print. The annotation is looked up by FQN string so the processor module does not have to
 * depend on `quarkdown-core` where `@Name` lives.
 */
internal object ModuleDescriber {
    /**
     * FQN of `@com.quarkdown.core.function.reflect.annotation.Name`.
     * Referenced as a string to avoid pulling `quarkdown-core` into the processor's classpath.
     */
    private const val QUARKDOWN_NAME_ANNOTATION_FQN = "com.quarkdown.core.function.reflect.annotation.Name"

    /**
     * Builds a [ModuleDescriptor] from a `@file:QModule` source by collecting its `@QFunction` declarations.
     * The module name is the file name without the `.kt` extension, matching the convention previously
     * encoded by the manually written `val Logger: QuarkdownModule = moduleOf(...)` declarations.
     */
    fun describe(file: KSFile): ModuleDescriptor {
        val functions =
            file.declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { it.hasAnnotation<QFunction>() }
                .map(::describe)
                .toList()

        return ModuleDescriptor(
            name = file.fileName.removeSuffix(".kt"),
            packageName = file.packageName.asString(),
            file = file,
            functions = functions,
        )
    }

    private fun describe(function: KSFunctionDeclaration): FunctionDescriptor {
        val originalName = function.simpleName.asString()
        return FunctionDescriptor(
            originalName = originalName,
            exportedName = function.annotations.findNameValue() ?: originalName,
            qualifiedName = function.qualifiedName?.asString() ?: originalName,
            returnType = function.returnType?.resolve() ?: error("Cannot resolve return type of '$originalName'"),
            parameters = function.parameters.map(::describe),
            declaration = function,
        )
    }

    private fun describe(parameter: KSValueParameter): ParameterDescriptor {
        val originalName = parameter.name?.asString() ?: error("Unnamed parameter in @QFunction is not supported")
        return ParameterDescriptor(
            originalName = originalName,
            exportedName = parameter.annotations.findNameValue() ?: originalName,
            type = parameter.type.resolve(),
        )
    }

    /**
     * Finds the `value` of a `@Name(...)` annotation in this annotation sequence, if present.
     * Returns `null` when no such annotation is attached.
     */
    private fun Sequence<KSAnnotation>.findNameValue(): String? =
        firstOrNull { annotation ->
            annotation.annotationType
                .resolve()
                .declaration.qualifiedName
                ?.asString() == QUARKDOWN_NAME_ANNOTATION_FQN
        }?.arguments
            ?.firstOrNull { it.name?.asString() == "name" || it.name == null }
            ?.value as? String
}
