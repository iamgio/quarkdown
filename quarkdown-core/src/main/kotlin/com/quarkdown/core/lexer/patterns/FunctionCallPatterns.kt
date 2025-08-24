package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.core.parser.walker.funcall.FunctionCallWalkerParser

/**
 * Patterns for block and inline function calls.
 */
class FunctionCallPatterns {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments.
     * Can be preceeded by the beginning of the line, a whitespace or a symbol.
     * This is a 'flag' pattern, meaning it does not capture any content,
     * but instead detects the beginning of a function call and delegates the scanning to [FunctionCallWalkerParser].
     */
    val inlineFunctionCall
        get() =
            TokenRegexPattern(
                name = "InlineFunctionCall",
                wrap = { FunctionCallToken(it, isBlock = false) },
                // The name of the function prefixed by a dot.
                regex =
                    RegexBuilder("(?<=before)(?=begin(name))")
                        .withReference("before", FUNCTION_CALL_PATTERN_BEFORE)
                        .withReference("begin", "\\" + FunctionCallGrammar.BEGIN)
                        .withReference("name", FunctionCallGrammar.IDENTIFIER_PATTERN)
                        .build(),
                // Arguments are scanned by the walker lexer.
                walker = { FunctionCallWalkerParser(it, allowsBody = false) },
            )

    /**
     * An isolated function call.
     * Function name prefixed by '.', followed by a sequence of arguments
     * and an optional body, indented by at least 2 spaces or 1 tab like a list item body.
     * This is a 'flag' pattern, meaning it does not capture any content,
     * but instead detects the beginning of a function call and delegates the scanning to [FunctionCallWalkerParser].
     */
    val blockFunctionCall
        get() =
            TokenRegexPattern(
                name = "FunctionCall",
                wrap = { FunctionCallToken(it, isBlock = true) },
                // The current operation to make sure the function call is not followed by other non-function content
                // is just checking if the line ends with an argument end character (}).
                // This works in most cases, but it should be improved soon with some better check.
                regex =
                    RegexBuilder("^ {0,3}callclose)")
                        .withReference("call", inlineFunctionCall.regex.pattern.dropLast(1))
                        .withReference("close", "(?:.*end)?\\s*\$")
                        .withReference("end", FunctionCallGrammar.ARGUMENT_END.toString())
                        .build(),
                // Arguments are scanned by the walker lexer.
                walker = { FunctionCallWalkerParser(it, allowsBody = true) },
            )
}

/**
 * Accepted pattern before a function call.
 */
const val FUNCTION_CALL_PATTERN_BEFORE = "^|\\s|[^a-zA-Z0-9.\\\\]"
