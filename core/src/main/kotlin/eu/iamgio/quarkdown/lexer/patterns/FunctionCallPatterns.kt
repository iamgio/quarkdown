package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.FunctionCallToken
import eu.iamgio.quarkdown.parser.walker.funcall.FunctionCallGrammar
import eu.iamgio.quarkdown.parser.walker.funcall.FunctionCallWalkerParser

/**
 * Patterns for block and inline function calls.
 */
class FunctionCallPatterns {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces.
     * Can be preceeded by the beginning of the line, a whitespace or a symbol.
     * Arguments are scanned by [FunctionCallArgumentsWalkerLexer].
     */
    val inlineFunctionCall
        get() =
            TokenRegexPattern(
                name = "InlineFunctionCall",
                wrap = { FunctionCallToken(it, isBlock = false) },
                // The name of the function prefixed by a dot.
                regex =
                    RegexBuilder("(?<=^|\\s|[^a-zA-Z0-9.\\\\])(?=begin(name))")
                        .withReference("begin", "\\" + FunctionCallGrammar.BEGIN)
                        .withReference("name", FunctionCallGrammar.IDENTIFIER_PATTERN)
                        .build(),
                // Arguments are scanned by the walker lexer.
                walker = { FunctionCallWalkerParser(it, allowsBody = false) },
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
                // The current operation to make sure the function call is not followed by other non-function content
                // is just checking if the line ends with an argument end character (}).
                // This works in most cases, but it should be improved soon with some better check.
                regex =
                    RegexBuilder("^ {0,3}call") // removed: (?=(?:.*})?\s*$) TODO
                        .withReference("call", inlineFunctionCall.regex.pattern)
                        .build(),
                // Arguments are scanned by the walker lexer.
                walker = { FunctionCallWalkerParser(it, allowsBody = true) },
            )
}
