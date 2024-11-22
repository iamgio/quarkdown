package eu.iamgio.quarkdown.parser.walker.funcall

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * Structured data produced by [FunctionCallWalkerParser] which represents a function call.
 * This syntax-only information will later be converted to a [FunctionCall] by [BlockTokenParser]
 * by injecting further context-aware information.
 * @param name the name of the function
 * @param arguments the function's arguments
 * @param bodyArgument the function's body argument, if any
 */
data class WalkedFunctionCall(
    val name: String,
    val arguments: List<WalkedArgument>,
    val bodyArgument: WalkedArgument?,
)

/**
 * Structured data produced by [FunctionCallWalkerParser] which represents a function call argument.
 * @param name the name of the argument, if the argument is named
 * @param value the raw value of the argument
 * @param isBody whether the argument is a body argument
 */
data class WalkedArgument(val name: String?, val value: String, val isBody: Boolean)
