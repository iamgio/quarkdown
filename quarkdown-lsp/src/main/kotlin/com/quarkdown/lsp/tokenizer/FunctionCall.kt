package com.quarkdown.lsp.tokenizer

import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.core.parser.walker.funcall.lastChainedCall

/**
 * A function call in Quarkdown source code.
 * @param range the range in the source code where this function call appears
 * @param tokens the list of tokens (parts) that make up this function call
 * @param parserResult the result of parsing this function call
 */
data class FunctionCall(
    val range: IntRange,
    val tokens: List<FunctionCallToken>,
    val parserResult: WalkerParsingResult<WalkedFunctionCall>,
) {
    /**
     * The name of the last function in a chain of function calls.
     *
     * For example, in `.func1 param:{value1}::func2 param:{value2}`, this would be `func2`.
     */
    val lastChainedName: String
        get() = parserResult.value.lastChainedCall.name
}

/**
 * A token within a function call which represents a specific part of the function call syntax
 * such as the function name, delimiters, argument values, etc.
 * @param type the type of this token, indicating its role in the function call
 * @param range the range in the source text where this token appears
 * @param lexeme the actual text of this token, which is the part of the source code
 */
data class FunctionCallToken(
    val type: Type,
    val range: IntRange,
    val lexeme: String,
) {
    /**
     * Represents the different types of tokens that can appear in a function call.
     */
    enum class Type {
        /** The beginning of a function call (typically `.`) */
        BEGIN,

        /** The name of the function being called. */
        FUNCTION_NAME,

        /** The separator for chaining function calls. */
        CHAINING_SEPARATOR,

        /** The name of a parameter in a named parameter. */
        PARAMETER_NAME,

        /** The delimiter between a parameter name and its value. */
        NAMED_PARAMETER_DELIMITER,

        /** The beginning of an inline argument. */
        INLINE_ARGUMENT_BEGIN,

        /** The content of an inline argument. */
        INLINE_ARGUMENT_VALUE,

        /** The end of an inline argument. */
        INLINE_ARGUMENT_END,

        /** A body argument. */
        BODY_ARGUMENT,
    }
}

/**
 * Finds the innermost function call that contains the specified index
 * (relative to the source code the function call was tokenized from).
 * @param index the source index to search for
 * @return the innermost function call containing the index, if any
 */
fun Iterable<FunctionCall>.getAtSourceIndex(index: Int): FunctionCall? =
    this
        .asSequence()
        .sortedBy { it.range.last - it.range.first } // Sorting by length, making sure to target innermost calls over their parent.
        .firstOrNull { index in it.range }

/**
 * Finds the token within a function call that contains the specified index
 * (relative to the source code the function call was tokenized from).
 * @param index the source index to search for
 * @return the token at the specified index, if any
 */
fun FunctionCall.getTokenAtSourceIndex(index: Int): FunctionCallToken? = this.tokens.find { index in it.range }
