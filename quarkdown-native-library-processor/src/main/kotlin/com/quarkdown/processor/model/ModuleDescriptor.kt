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
 * @param sourceImports verbatim text of the source file's import list, or `null` when the file has none;
 *                      copied into the generated wrapper so default expressions can reference symbols the source imported
 */
data class ModuleDescriptor(
    val name: String,
    val packageName: String,
    val file: KSFile,
    val functions: List<FunctionDescriptor>,
    val sourceImports: String?,
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
 * @param sourceAnnotations verbatim source text of function-level annotations to propagate to the wrapper, or `null` when the function has none
 * @param kdoc raw KDoc block text of the source function (inner content, no `/** */` markers), or `null` when the function is undocumented;
 *             copied to the wrapper by the code generator after names have been substituted
 */
data class FunctionDescriptor(
    val originalName: String,
    val exportedName: String,
    val qualifiedName: String,
    val returnType: KSType,
    val parameters: List<ParameterDescriptor>,
    val declaration: KSFunctionDeclaration,
    val sourceAnnotations: String?,
    val kdoc: String?,
)

/**
 * A parameter of an exported [FunctionDescriptor].
 *
 * Sealed so that each variant represents a distinct contribution to the wrapper:
 * - [Plain] maps 1:1 to the source function's parameter list;
 * - [Spread] expands a class type into one wrapper parameter per constructor member and
 *   rebuilds the instance at the delegation call site.
 *
 * The code generator flat-maps every parameter to its Plain contributions when assembling the
 * wrapper's signature and switches on the concrete variant when assembling the delegation.
 */
sealed class ParameterDescriptor {
    /** Kotlin parameter name on the source function (the name used as the delegation argument label). */
    abstract val originalName: String

    /** Verbatim source text of parameter-level annotations to propagate to the wrapper, or `null` when none apply. */
    abstract val sourceAnnotations: String?

    /**
     * A plain parameter: exactly one entry in the source function's parameter list, exactly one
     * entry in the wrapper's parameter list.
     *
     * @param originalName Kotlin parameter name on the source function
     * @param exportedName name to use on the generated wrapper, possibly rewritten by `@Name`
     * @param type the resolved parameter type
     * @param defaultExpression verbatim source text of the parameter's default value, or `null` when the parameter has no default
     * @param sourceAnnotations verbatim source text of parameter-level annotations to propagate to the wrapper
     */
    data class Plain(
        override val originalName: String,
        val exportedName: String,
        val type: KSType,
        val defaultExpression: String?,
        override val sourceAnnotations: String?,
    ) : ParameterDescriptor()

    /**
     * A spread parameter: the source function declared one parameter of a class type marked with
     * `@Spread`; the wrapper exposes one [Plain] per primary-constructor member of that class
     * and reconstructs an instance via named-argument constructor invocation at the delegation.
     *
     * @param originalName Kotlin parameter name of the outer spread parameter on the source function
     * @param dataClassFqn fully qualified name of the class being spread, used to emit the reconstruction call
     * @param components one [Plain] per primary-constructor parameter of the spread class, in declaration order
     * @param sourceAnnotations verbatim source text of the outer parameter's annotations
     *                          (the processor filters `@Spread` out; anything else is retained but currently not emitted since the outer parameter itself is not part of the wrapper signature)
     * @param dataClassKdoc raw KDoc block text of the spread class (inner content, no `/** */` markers), or `null` when the class is undocumented;
     *                       the wrapper KDoc absorbs its `@param` tags in place of the outer parameter's own documentation
     */
    data class Spread(
        override val originalName: String,
        val dataClassFqn: String,
        val components: List<Plain>,
        override val sourceAnnotations: String?,
        val dataClassKdoc: String?,
    ) : ParameterDescriptor()
}
