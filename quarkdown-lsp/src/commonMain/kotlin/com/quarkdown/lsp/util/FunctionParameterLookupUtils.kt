package com.quarkdown.lsp.util

import com.quarkdown.core.parser.walker.funcall.WalkedFunctionArgument
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.core.util.offset
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter

/**
 * Finds the argument at the given source index in this function call.
 * Note that, by design, the range is between the argument's delimiters, excluding the parameter name if any.
 * @param index the source index where the argument appears
 * @return the argument at the given index, if any
 */
fun FunctionCall.getArgumentAtSourceIndex(index: Int): WalkedFunctionArgument? =
    this.parserResult.value.arguments
        .find { index in it.range.offset(this.range.first) }

/**
 * Finds the parameter corresponding to the argument at the given source index in this function call.
 * The argument can be either named or positional, and the lookup happens against the provided function documentation.
 * @param function the documented function whose parameters are to be searched
 * @param index the source index where the argument appears
 * @return the parameter corresponding to the argument, if any
 */
fun FunctionCall.getParameterAtSourceIndex(
    function: DocsFunction,
    index: Int,
): DocsParameter? {
    val parameters = function.parameters.takeIf { it.isNotEmpty() } ?: return null
    val argument: WalkedFunctionArgument = this.getArgumentAtSourceIndex(index) ?: return null

    // The parameter whose value being completed, either named or positional.
    return getParameterByName(argument.name, parameters)
        ?: getParameterByPosition(this.parserResult.value, argument, parameters)
}

/**
 * Finds a parameter by its name.
 * @param name the name of the parameter to find
 * @param parameters the list of parameters to search in
 * @return the parameter with the given name, if any
 */
private fun getParameterByName(
    name: String?,
    parameters: List<DocsParameter>,
): DocsParameter? =
    name?.let { paramName ->
        parameters.find { it.name == paramName }
    }

/**
 * Finds a parameter by the position of its corresponding argument in the function call.
 * Note that all arguments before the target argument must be positional as well.
 * If a named argument appears before the target argument, `null` is returned.
 * @param call the function call containing the argument
 * @param argument the argument whose parameter to find
 * @param parameters the list of parameters to search in
 * @return the parameter corresponding to the argument's position, if any
 */
private fun getParameterByPosition(
    call: WalkedFunctionCall,
    argument: WalkedFunctionArgument,
    parameters: List<DocsParameter>,
): DocsParameter? {
    for ((index, arg) in call.arguments.withIndex()) {
        // A positional argument must appear before any named one.
        if (arg.name != null) return null
        // Match by position.
        if (arg == argument) return parameters.getOrNull(index)
    }
    return null
}
