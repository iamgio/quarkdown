package com.quarkdown.core.function.reflect

import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.factory.ValueFactory
import kotlin.reflect.KClass

/**
 * Marks a [ValueFactory] function as the conversion from a raw or [DynamicValue] argument into a
 * specific static type.
 *
 * This annotation is read at build time by the native library processor, which bakes the matching
 * call into each generated function parameter.
 *
 * @param unwrappedType static type this function produces; a parameter is converted by this
 *                      function when its type is [unwrappedType] or a subtype of it
 * @param requiresContext whether the function takes the call's context as a second argument
 */
@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class FromDynamicType(
    val unwrappedType: KClass<*>,
    val requiresContext: Boolean = false,
)
