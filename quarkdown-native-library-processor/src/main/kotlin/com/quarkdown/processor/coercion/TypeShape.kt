package com.quarkdown.processor.coercion

/**
 * The parts of a parameter's resolved type that the [CoercionPlanner] needs.
 * @param qualifiedName fully qualified name of the type itself
 * @param simpleName simple name, used as the parameter's display name in errors and signatures
 * @param supertypeQualifiedNames fully qualified names of every transitive supertype
 * @param isEnum whether the type is an enum class
 * @param isNullable whether the parameter's type is marked nullable
 */
data class TypeShape(
    val qualifiedName: String,
    val simpleName: String,
    val supertypeQualifiedNames: Set<String>,
    val isEnum: Boolean,
    val isNullable: Boolean,
)
