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
    ): Regex {
        return RegexBuilder("mmath|" + super.interruptionRule(includeList, includeTable).pattern)
            .withReference("mmath", " {0,3}(?:\\\${3,})[^\\n]*\\n")
            .build()
    }

    /**
     * Three or more `<` characters not followed by any other character.
     * Indicates a page break.
     */
    val pageBreak
        get() =
            TokenRegexPattern(
                name = "PageBreak",
                wrap = ::PageBreakToken,
                regex =
                    "^ {0,3}<{3,}(?=\\s*\$)"
                        .toRegex(),
            )

    /**
     * Fenced content within triple dollar signs.
     * @see MultilineMathToken
     */
    val multilineMath
        get() =
            TokenRegexPattern(
                name = "MultilineMath",
                wrap = ::MultilineMathToken,
                regex =
                    "^ {0,3}(\\\${3,})((.|\\s)+?)(\\\${3,})"
                        .toRegex(),
            )

    /**
     * Fenced content within spaced dollar signs on the same line.
     * @see OnelineMathToken
     */
    val onelineMath
        get() =
            TokenRegexPattern(
                name = "OnelineMath",
                wrap = ::OnelineMathToken,
                regex =
                    RegexBuilder("^ {0,3}math\\s*\$")
                        .withReference("math", ONELINE_MATH_HELPER)
                        .build(),
            )

    /**
     * An isolated function call.
     * Function name prefixed by '.', followed by a sequence of arguments wrapped in curly braces
     * and an optional body, indented by 4 spaces like a list item body.
     */
    val functionCall
        get() = FunctionCallPatterns().blockFunctionCall
}

/**
 * Pattern of one-line fenced content between two dollar signs.
 * The spacing between the dollar signs and the inner content must be of one unit.
 */
const val ONELINE_MATH_HELPER = "\\\$[ \\t](.+?)(?<![ \\t])[ \\t]\\\$"
