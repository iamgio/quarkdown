package com.quarkdown.core.function

import com.quarkdown.core.function.call.binding.ArgumentConverter

/**
 * A declared [Function] parameter.
 * @param name name of the parameter
 * @param type kind of value this parameter accepts
 * @param index index of the parameter in the function signature
 * @param isOptional whether the corresponding argument in a function call can be omitted
 * @param isExplicitlyBody whether the parameter is explicitly reserved for the body argument, even if it's not the last one in the signature
 * @param isInjected whether the corresponding argument in a function call is automatically injected
 *                   and is not to be supplied by the caller.
 * @param isNullable whether the parameter accepts `null` values.
 *                   When `true`, [com.quarkdown.core.function.value.NoneValue] arguments are accepted
 *                   and converted to Kotlin's `null` at invocation time.
 * @param convert conversion to the parameter's static type, applied while binding.
 *                `null` for hand-built functions, whose invocation reads argument values directly.
 */
data class FunctionParameter(
    val name: String,
    val type: ParameterType,
    val index: Int,
    val isOptional: Boolean = false,
    val isExplicitlyBody: Boolean = false,
    val isInjected: Boolean = false,
    val isNullable: Boolean = false,
    val convert: ArgumentConverter? = null,
)
