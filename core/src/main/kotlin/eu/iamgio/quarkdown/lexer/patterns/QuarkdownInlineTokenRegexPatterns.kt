package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.FunctionCallToken
import eu.iamgio.quarkdown.lexer.InlineMathToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * Regex patterns for [eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor].
 */
class QuarkdownInlineTokenRegexPatterns : BaseMarkdownInlineTokenRegexPatterns() {
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
                    RegexBuilder("\\.(\\w+)" + "arg".repeat(FUNCTION_MAX_ARG_COUNT))
                        .withReference("arg", "$FUNCTION_ARGUMENT_HELPER?")
                        .build(),
            )

    /**
     * Fenced content within spaced dollar signs on the same line.
     * @see InlineMathToken
     */
    val inlineMath
        get() =
            TokenRegexPattern(
                name = "InlineMath",
                wrap = ::InlineMathToken,
                regex =
                    RegexBuilder("(?:^|\\s+)math\\s+")
                        .withReference("math", ONELINE_MATH_HELPER)
                        .build(),
            )
}

private const val FUNCTION_MAX_ARG_COUNT = 10

private const val FUNCTION_ARGUMENT_HELPER = "(?:\\s*\\{(.+?)})"
