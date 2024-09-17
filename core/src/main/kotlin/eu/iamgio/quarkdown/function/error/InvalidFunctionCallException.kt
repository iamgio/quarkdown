package eu.iamgio.quarkdown.function.error

import eu.iamgio.quarkdown.BAD_FUNCTION_CALL_EXIT_CODE
import eu.iamgio.quarkdown.ast.dsl.buildInline
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.asString
import eu.iamgio.quarkdown.function.signatureAsString

private const val TEXT_AUTOCOLLAPSE_MAX_LENGTH = 40

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
                    text("Cannot call function ${call.function.name}")
                    // If the signature is too long, it is collapsed by default and can be expanded by the user.
                    autoCollapse(
                        text = call.function.signatureAsString(includeName = false),
                        maxLength = TEXT_AUTOCOLLAPSE_MAX_LENGTH,
                    )

                    if (includeArguments) {
                        text(" with arguments ")
                        // The same applies to arguments.
                        autoCollapse(
                            text = call.arguments.asString(),
                            maxLength = TEXT_AUTOCOLLAPSE_MAX_LENGTH,
                        )
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
