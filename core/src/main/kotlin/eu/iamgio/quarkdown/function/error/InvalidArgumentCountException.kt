package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.function.FunctionCall

/**
 * An exception thrown if the amount of arguments and mandatory parameters of a function call does not match.
 * @param call the invalid call
 */
class InvalidArgumentCountException(call: FunctionCall<*>) :
    InvalidFunctionCallException(
        call,
        reason = "expected ${call.function.parameters.size} arguments, but ${call.arguments.size} found",
    )
