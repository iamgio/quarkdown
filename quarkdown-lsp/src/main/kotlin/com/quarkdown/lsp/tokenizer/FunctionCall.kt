package com.quarkdown.lsp.tokenizer

import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall

/**
 * Represents a function call in Quarkdown text.
 * 
 * A function call consists of a range in the source text, a list of tokens that make up
 * the function call, and the parser result containing detailed information about the call.
 * 
 * Function calls in Quarkdown have the format: `.functionName parameter:{value}` or
 * `.functionName {inlineArgument}`.
 */
data class FunctionCall(
    val range: IntRange,
    val tokens: List<FunctionCallToken>,
    val parserResult: WalkerParsingResult<WalkedFunctionCall>,
)

/**
 * Represents a token within a function call.
 * 
 * Each token has a specific type, a range in the source text, and the actual text (lexeme)
 * that the token represents.
 */
data class FunctionCallToken(
    /** The type of this token */
    val type: Type,
    /** The range in the source text where this token appears */
    val range: IntRange,
    /** The actual text of this token */
    val lexeme: String,
) {
    /**
     * Represents the different types of tokens that can appear in a function call.
     */
    enum class Type {
        /** The beginning of a function call (typically '.') */
        BEGIN,
        /** The name of the function being called */
        FUNCTION_NAME,
        /** The name of a parameter in a named parameter */
        PARAMETER_NAME,
        /** The delimiter between a parameter name and its value (typically ':') */
        NAMED_PARAMETER_DELIMITER,
        /** The beginning of an inline argument (typically '{') */
        INLINE_ARGUMENT_BEGIN,
        /** The content of an inline argument */
        INLINE_ARGUMENT_VALUE,
        /** The end of an inline argument (typically '}') */
        INLINE_ARGUMENT_END,
        /** A body argument (typically a block of text) */
        BODY_ARGUMENT,
    }
}
