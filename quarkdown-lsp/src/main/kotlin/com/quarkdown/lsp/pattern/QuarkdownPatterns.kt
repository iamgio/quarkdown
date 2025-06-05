package com.quarkdown.lsp.pattern

import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
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
         * The character that begins an inline argument.
         */
        const val ARGUMENT_BEGIN = FunctionCallGrammar.ARGUMENT_BEGIN

        /**
         * The character that ends an inline argument.
         */
        const val ARGUMENT_END = FunctionCallGrammar.ARGUMENT_END

        /**
         * The character that delimits a named argument.
         */
        const val NAMED_ARGUMENT_DELIMITER = FunctionCallGrammar.NAMED_ARGUMENT_DELIMITER

        /**
         * The pattern that matches the identifier in a function call, preceded by [BEGIN] (unmatched).
         */
        val identifierInCall: Regex = "(?<=${Regex.escape(BEGIN)})(${IDENTIFIER.pattern})".toRegex()
    }
}
