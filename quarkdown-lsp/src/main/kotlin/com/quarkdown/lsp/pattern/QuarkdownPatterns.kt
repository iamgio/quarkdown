package com.quarkdown.lsp.pattern

import com.quarkdown.core.lexer.patterns.FUNCTION_CALL_PATTERN_BEFORE
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.lsp.pattern.QuarkdownPatterns.FunctionCall.ARGUMENT_BEGIN
import com.quarkdown.lsp.pattern.QuarkdownPatterns.FunctionCall.BEGIN

/**
 * Patterns used by the Quarkdown lexer.
 */
object QuarkdownPatterns {
    /**
     * Patterns related to function calls.
     */
    object FunctionCall {
        /**
         * The character that prefixes a function call.
         */
        const val BEGIN: String = FunctionCallGrammar.BEGIN

        /**
         * The pattern for an identifier (function name or argument name).
         */
        val IDENTIFIER: Regex = FunctionCallGrammar.IDENTIFIER_PATTERN.toRegex()

        /**
         * The pattern that chains function calls together.
         */
        const val CHAIN_SEPARATOR: String = FunctionCallGrammar.CHAIN_SEPARATOR

        /**
         * The character that begins an inline argument.
         */
        const val ARGUMENT_BEGIN = FunctionCallGrammar.ARGUMENT_BEGIN.toString()

        /**
         * The character that ends an inline argument.
         */
        const val ARGUMENT_END = FunctionCallGrammar.ARGUMENT_END.toString()

        /**
         * The character that delimits a named argument.
         */
        const val NAMED_ARGUMENT_DELIMITER = FunctionCallGrammar.NAMED_ARGUMENT_DELIMITER

        /**
         * Default/suggested indentation for the body argument of a function call.
         */
        const val CONVENTIONAL_BODY_INDENT = "    "

        /**
         * The pattern that matches an optional identifier in a function call, preceded by [BEGIN] (unmatched).
         */
        val identifierInCall: Regex = "(?<=($FUNCTION_CALL_PATTERN_BEFORE)${Regex.escape(BEGIN)})($IDENTIFIER)?".toRegex()

        /**
         * The pattern that matches an optional value (represented by an identifier pattern) in an incomplete inline argument,
         * preceded by [ARGUMENT_BEGIN] (unmatched) and followed by the end of the string.
         */
        val optionalValueInArgument: Regex =
            "(?<=${Regex.escape(ARGUMENT_BEGIN)})\\s*($IDENTIFIER)?$".toRegex()
    }
}
