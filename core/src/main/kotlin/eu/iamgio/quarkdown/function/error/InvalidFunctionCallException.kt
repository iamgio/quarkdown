package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.BAD_FUNCTION_CALL_EXIT_CODE
import eu.iamgio.quarkdown.ast.dsl.buildInline
import eu.iamgio.quarkdown.function.asString
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.asString

/**
 * An exception thrown if a [FunctionCall] could not be executed.
 * @param call the invalid call
 * @param reason optional additional reason the call failed for
 * @param includeArguments whether to include supplied function call arguments in the error message
 */
open class InvalidFunctionCallException(
    val call: FunctionCall<*>,
    reason: String? = null,
    includeArguments: Boolean = true,
) :
    FunctionException(
            richMessage =
                buildInline {
                    text("Cannot call function ")
                    emphasis { text(call.function.asString()) }
                    if (includeArguments) {
                        text(" with arguments ")
                        emphasis { text(call.arguments.asString()) }
                    }
                    reason?.let {
                        text(": ")
                        lineBreak()
                        emphasis { text(it) }
                    }
                },
            code = BAD_FUNCTION_CALL_EXIT_CODE,
            function = call.function,
        )
