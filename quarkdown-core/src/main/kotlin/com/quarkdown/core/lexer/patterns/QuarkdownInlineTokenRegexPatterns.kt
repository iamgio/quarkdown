package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.InlineMathToken

/**
 * Regex patterns for [com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor].
 */
class QuarkdownInlineTokenRegexPatterns : BaseMarkdownInlineTokenRegexPatterns() {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces.
     */
    val inlineFunctionCall by lazy {
        FunctionCallPatterns().inlineFunctionCall
    }

    /**
     * Fenced content within spaced dollar signs on the same line.
     * @see InlineMathToken
     */
    val inlineMath by lazy {
        TokenRegexPattern(
            name = "InlineMath",
            wrap = ::InlineMathToken,
            regex =
                RegexBuilder("(?<=^|\\s|\\W)math(?=\$|\\s|\\W)")
                    .withReference("math", ONELINE_MATH_HELPER)
                    .build(),
        )
    }

    /**
     * Patterns for sequences of characters that correspond to text symbols.
     */
    val textReplacements: List<TokenRegexPattern> = TextSymbolReplacement.entries.map { it.toTokenPattern() }
}
