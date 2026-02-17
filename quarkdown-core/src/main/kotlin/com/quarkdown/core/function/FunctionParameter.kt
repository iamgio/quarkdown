package com.quarkdown.core.function

import kotlin.reflect.KClass

/**
 * A declared [Function] parameter.
 * @param name name of the parameter
 * @param type expected input value type
 * @param index index of the parameter in the function signature
 * @param isOptional whether the corresponding argument in a function call can be omitted
 * @param isInjected whether the corresponding argument in a function call is automatically injected
 *                   and is not to be supplied by the caller.
 * @param isNullable whether the parameter accepts `null` values.
 *                   When `true`, [com.quarkdown.core.function.value.NoneValue] arguments are accepted
 *                   and converted to Kotlin's `null` at invocation time.
 * @param T input type of the parameter
 */
data class FunctionParameter<T : Any>(
    val name: String,
    val type: KClass<T>,
    val index: Int,
    val isOptional: Boolean = false,
    // When a function parameter is loaded from a KFunction via KFunctionAdapter,
    // a parameter is injected if it's annotated with `@Injected`
    val isInjected: Boolean = false,
    // When a function parameter is loaded from a KFunction via KFunctionAdapter,
    // a parameter is nullable if the corresponding KParameter type is marked nullable.
    val isNullable: Boolean = false,
)
