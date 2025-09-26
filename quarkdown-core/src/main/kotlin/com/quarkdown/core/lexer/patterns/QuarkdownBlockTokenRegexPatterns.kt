package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.MultilineMathToken
import com.quarkdown.core.lexer.tokens.OnelineMathToken
import com.quarkdown.core.lexer.tokens.PageBreakToken

/**
 * Regex patterns for [com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor].
 */
class QuarkdownBlockTokenRegexPatterns : BaseMarkdownBlockTokenRegexPatterns() {
    override fun interruptionRule(
        includeList: Boolean,
        includeTable: Boolean,
    ): Regex =
        RegexBuilder("mmath|" + super.interruptionRule(includeList, includeTable).pattern)
            .withReference("mmath", " {0,3}(?:\\\${3,})[^\\n]*\\n")
            .build()

    /**
     * Three or more `<` characters not followed by any other character.
     * Indicates a page break.
     */
    val pageBreak by lazy {
        TokenRegexPattern(
            name = "PageBreak",
            wrap = ::PageBreakToken,
            regex =
                "^ {0,3}<{3,}(?=\\s*\$)"
                    .toRegex(),
        )
    }

    /**
     * Fenced content within triple dollar signs.
     * @see MultilineMathToken
     */
    val multilineMath by lazy {
        TokenRegexPattern(
            name = "MultilineMath",
            wrap = ::MultilineMathToken,
            regex =
                RegexBuilder("^ {0,3}header((.|\\s)+?)fencesend[ \\t]*$")
                    .withReference("header", "fencesstart[ \\t]*customid?[ \\t]*$")
                    .withReference("fencesstart", "\\\${3,}")
                    .withReference("fencesend", "\\\${3,}")
                    .withReference("customid", PatternHelpers.customId("multilinemath"))
                    .build(),
            groupNames = listOf("multilinemathcustomid"),
        )
    }

    /**
     * Fenced content within spaced dollar signs on the same line,
     * with optional custom ID for cross-referencing.
     * @see OnelineMathToken
     */
    val onelineMath by lazy {
        TokenRegexPattern(
            name = "OnelineMath",
            wrap = ::OnelineMathToken,
            regex =
                RegexBuilder("^ {0,3}math[ \\t]*customid?[ \\t]*$")
                    .withReference("math", PatternHelpers.ONELINE_MATH)
                    .withReference("customid", PatternHelpers.customId("onelinemath"))
                    .build(),
            groupNames = listOf("onelinemathcustomid"),
        )
    }

    /**
     * An isolated function call.
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces
     * and an optional body, indented by 4 spaces like a list item body.
     */
    val functionCall by lazy {
        FunctionCallPatterns().blockFunctionCall
    }
}
