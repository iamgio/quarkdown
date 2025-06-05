package com.quarkdown.core.function.error

import com.quarkdown.core.function.FunctionParameter
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.call.asString

/**
 * An exception thrown if a function parameter is bound more than once in a function call.
 * @param call the invalid call
 * @param parameter the parameter that was attempted to be bound again
 * @param overriddingArgument the argument that was attempted to be bound to the already bound parameter
 */
class ParameterAlreadyBoundException(
    call: FunctionCall<*>,
    parameter: FunctionParameter<*>,
    overriddingArgument: FunctionCallArgument,
) : InvalidFunctionCallException(
        call,
        reason = "parameter '${parameter.name}' is already bound, but was attempted to be bound again to ${overriddingArgument.asString()}",
    )
