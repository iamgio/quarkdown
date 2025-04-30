package com.quarkdown.core.function.error

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument

/**
 * An exception thrown if a parameter-argument pair of a function call has incompatible types.
 * @param call the invalid call
 */
class MismatchingArgumentTypeException(
    call: FunctionCall<*>,
    parameter: FunctionParameter<*>,
    argument: FunctionCallArgument,
) : InvalidFunctionCallException(
        call,
        reason =
            "expected type ${parameter.type.simpleName} for parameter '${parameter.name}', " +
                "but ${argument.value::class.simpleName} found",
    )
