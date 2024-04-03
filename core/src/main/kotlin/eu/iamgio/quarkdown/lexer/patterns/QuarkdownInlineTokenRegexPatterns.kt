package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.InlineMathToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * Regex patterns for [eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor].
 */
class QuarkdownInlineTokenRegexPatterns : BaseMarkdownInlineTokenRegexPatterns() {
    /**
     * Fenced content within spaced dollar signs on the same line.
     * @see InlineMathToken
     */
    val inlineMath
        get() =
            TokenRegexPattern(
                name = "InlineMath",
                ::InlineMathToken,
                regex =
                    RegexBuilder("(?:^|\\s+)math\\s+")
                        .withReference("math", ONELINE_MATH_HELPER)
                        .build(),
            )
}
