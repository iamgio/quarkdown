package com.quarkdown.processor.coercion

/**
 * One `@FromDynamicType`-annotated function on `ValueFactory`, as read off the compile classpath.
 *
 * @param functionName simple name of the factory function, e.g. `size`
 * @param unwrappedTypeQualifiedName the annotation's `unwrappedType`, which a parameter type must be a subtype of
 * @param requiresContext whether the function takes the call's context as a second argument
 */
data class FactoryCandidate(
    val functionName: String,
    val unwrappedTypeQualifiedName: String,
    val requiresContext: Boolean,
)
