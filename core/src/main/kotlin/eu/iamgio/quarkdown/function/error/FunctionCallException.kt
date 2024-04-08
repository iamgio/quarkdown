package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.BAD_FUNCTION_CALL_EXIT_CODE
import eu.iamgio.quarkdown.function.FunctionCall
import eu.iamgio.quarkdown.function.FunctionCallArgument
import eu.iamgio.quarkdown.function.FunctionParameter
import eu.iamgio.quarkdown.function.asString
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * An exception thrown if a [FunctionCall] could not be executed.
 * @param call the invalid call
 * @param reason optional additional reason the call failed for
 */
open class InvalidFunctionCallException(call: FunctionCall<*>, reason: String? = null) :
    PipelineException(
        "Cannot call function ${call.function.asString()} with arguments ${call.arguments.asString()}" +
            (reason?.let { ": $it" } ?: ""),
        code = BAD_FUNCTION_CALL_EXIT_CODE,
    )

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
 * An exception thrown if a parameter-argument pair of a function call has incompatible types.
 * @param call the invalid call
 */
class MismatchingArgumentTypeException(
    call: FunctionCall<*>,
    parameter: FunctionParameter<*>,
    argument: FunctionCallArgument<*>,
) : InvalidFunctionCallException(
        call,
        reason =
            "expected type ${parameter.type.simpleName} for parameter '${parameter.name}', " +
                "but ${argument.value::class.simpleName} found",
    )
