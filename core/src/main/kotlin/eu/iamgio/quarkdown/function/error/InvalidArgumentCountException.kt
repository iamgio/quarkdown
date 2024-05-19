package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.BAD_FUNCTION_CALL_EXIT_CODE
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * An exception thrown if the amount of arguments and mandatory parameters of a function call does not match.
 * @param call the invalid call
 */
class InvalidArgumentCountException(call: FunctionCall<*>) :
    InvalidFunctionCallException(
        call,
        reason = "expected ${call.function.parameters.size} arguments, but ${call.arguments.size} found",
    )

/**
 * An exception thrown if the amount of arguments and mandatory parameters of a lambda block does not match.
 * @param argumentCount given argument count
 * @param parameterCount expected parameter count
 */
class InvalidLambdaArgumentCountException(argumentCount: Int, parameterCount: Int) :
    PipelineException(
        "Lambda expects $parameterCount arguments, but $argumentCount found",
        BAD_FUNCTION_CALL_EXIT_CODE,
    )
