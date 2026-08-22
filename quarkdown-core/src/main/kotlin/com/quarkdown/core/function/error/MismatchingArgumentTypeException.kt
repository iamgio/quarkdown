package com.quarkdown.core.function.error

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall

/**
 * An exception thrown if a parameter-argument pair of a function call has incompatible types.
 * @param call the invalid call
 * @param parameter the parameter that could not accept the argument
 * @param found the offending value, either a wrapped [com.quarkdown.core.function.value.Value] or a raw one
 */
class MismatchingArgumentTypeException(
    call: FunctionCall<*>,
    parameter: FunctionParameter,
    found: Any?,
) : InvalidFunctionCallException(
        call,
        reason =
            "expected type ${parameter.type.displayName} for parameter '${parameter.name}', " +
                "but ${found?.let { it::class.simpleName } ?: "nothing"} found",
    )
