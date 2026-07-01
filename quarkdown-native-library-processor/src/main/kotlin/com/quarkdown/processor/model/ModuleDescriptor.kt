package com.quarkdown.processor.model

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * A native Quarkdown module discovered by the KSP processor.
 *
 * Each descriptor corresponds to a single source file annotated with [com.quarkdown.processor.annotation.QModule].
 *
 * @param name simple name of the module
 * @param packageName JVM package of the source file
 * @param file the underlying KSP file declaration
 * @param functions exported functions discovered in this module, in source order
 */
data class ModuleDescriptor(
    val name: String,
    val packageName: String,
    val file: KSFile,
    val functions: List<FunctionDescriptor>,
)

/**
 * A native Quarkdown function discovered within a [ModuleDescriptor].
 *
 * Corresponds to a top-level Kotlin function annotated with
 * [com.quarkdown.processor.annotation.QFunction] inside a [com.quarkdown.processor.annotation.QModule] file.
 *
 * @param originalName Kotlin simple name of the source function (used to invoke the original from the wrapper)
 * @param exportedName name exposed to Quarkdown after applying `@Name` to the function; falls back to [originalName] when absent
 * @param qualifiedName fully qualified name of the source function
 * @param returnType resolved return type of the source function
 * @param parameters function parameters in declaration order
 * @param declaration the underlying KSP declaration
 */
data class FunctionDescriptor(
    val originalName: String,
    val exportedName: String,
    val qualifiedName: String,
    val returnType: KSType,
    val parameters: List<ParameterDescriptor>,
    val declaration: KSFunctionDeclaration,
)

/**
 * A single parameter of an exported [FunctionDescriptor].
 *
 * @param originalName Kotlin parameter name on the source function
 * @param exportedName name to use on the generated wrapper, possibly rewritten by `@Name`
 * @param type the resolved parameter type
 */
data class ParameterDescriptor(
    val originalName: String,
    val exportedName: String,
    val type: KSType,
)
