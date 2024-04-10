package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.FunctionCallToken

/**
 * Patterns for block and inline function calls.
 */
class FunctionCallPatterns {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces.
     */
    val inlineFunctionCall
        get() =
            TokenRegexPattern(
                name = "InlineFunctionCall",
                wrap = ::FunctionCallToken,
                // Repeating groups can't be captured, hence a capped repeated patterns is used.
                regex =
                    RegexBuilder("\\.(\\w+)args")
                        .withReference("args", FUNCTION_ARGUMENT_HELPER.repeat(FUNCTION_MAX_ARG_COUNT))
                        .build(),
            )

    /**
     * An isolated function call.
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces
     * and an optional body, indented by 4 spaces like a list item body.
     */
    val blockFunctionCall
        get() =
            TokenRegexPattern(
                name = "FunctionCall",
                wrap = ::FunctionCallToken,
                regex =
                    RegexBuilder("^ {0,3}call(body)")
                        .withReference("call", inlineFunctionCall.regex.pattern)
                        .withReference("body", "(?:\\s*^ {2,}.+)*")
                        .build(),
            )
}

/**
 * Max amount of arguments supported.
 */
private const val FUNCTION_MAX_ARG_COUNT = 10

/**
 * Regular argument pattern of a function call (not body arguments).
 */
private const val FUNCTION_ARGUMENT_HELPER = "(?:\\s*\\{\\s*(.+?)\\s*})?"
// TODO allow args in nested function calls (match balanced parentheses)
