package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.InlineMathToken

/**
 * Regex patterns for [eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor].
 */
class QuarkdownInlineTokenRegexPatterns : BaseMarkdownInlineTokenRegexPatterns() {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces.
     */
    val inlineFunctionCall
        get() = FunctionCallPatterns().inlineFunctionCall

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
                    RegexBuilder("(?<=^|\\s|\\W)math(?=\$|\\s|\\W)")
                        .withReference("math", ONELINE_MATH_HELPER)
                        .build(),
            )

    /**
     * Patterns for sequences of characters that correspond to text symbols.
     */
    val textReplacements: List<TokenRegexPattern> = TextSymbolReplacement.entries.map { it.toTokenPattern() }
}
