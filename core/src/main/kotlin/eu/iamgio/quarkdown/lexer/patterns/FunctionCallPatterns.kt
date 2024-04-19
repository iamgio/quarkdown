package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.FunctionCallToken
import eu.iamgio.quarkdown.lexer.walker.FunctionCallArgumentsWalkerLexer

/**
 * Patterns for block and inline function calls.
 */
class FunctionCallPatterns {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces.
     * Arguments are scanned by [FunctionCallArgumentsWalkerLexer].
     */
    val inlineFunctionCall
        get() =
            TokenRegexPattern(
                name = "InlineFunctionCall",
                wrap = { FunctionCallToken(it, isBlock = false) },
                // The name of the function prefixed by a dot.
                regex =
                    "(?<=\\s|^)\\.(\\w+)"
                        .toRegex(),
                // Arguments are scanned by the walker lexer.
                walker = { FunctionCallArgumentsWalkerLexer(it, allowsBody = false) },
            )

    /**
     * An isolated function call.
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces
     * and an optional body, indented by 4 spaces like a list item body.
     * Arguments are scanned by [FunctionCallArgumentsWalkerLexer].
     */
    val blockFunctionCall
        get() =
            TokenRegexPattern(
                name = "FunctionCall",
                wrap = { FunctionCallToken(it, isBlock = true) },
                regex =
                    RegexBuilder("^ {0,3}call")
                        .withReference("call", inlineFunctionCall.regex.pattern)
                        .build(),
                // Arguments are scanned by the walker lexer.
                walker = { FunctionCallArgumentsWalkerLexer(it, allowsBody = true) },
            )
}
