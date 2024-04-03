package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.MultilineMathToken
import eu.iamgio.quarkdown.lexer.OnelineMathToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * Regex patterns for [eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor].
 */
class QuarkdownBlockTokenRegexPatterns : BaseMarkdownBlockTokenRegexPatterns() {
    override fun interruptionRule(
        includeList: Boolean,
        includeTable: Boolean,
    ): Regex {
        return RegexBuilder("mmath|omath|" + super.interruptionRule(includeList, includeTable).pattern)
            .withReference("mmath", " {0,3}(?:\\\${3,})[^\\n]*\\n")
            .withReference("omath", onelineMath.regex.pattern)
            .build()
    }

    /**
     * Fenced content within triple dollar signs.
     * @see MultilineMathToken
     */
    val multilineMath
        get() =
            TokenRegexPattern(
                name = "MultilineMath",
                ::MultilineMathToken,
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
                ::OnelineMathToken,
                regex =
                    RegexBuilder("^ {0,3}math\\s*\$")
                        .withReference("math", ONELINE_MATH_HELPER)
                        .build(),
            )
}

/**
 * Pattern of one-line fenced content between two dollar signs.
 * The spacing between the dollar signs and the inner content must be of one unit.
 */
const val ONELINE_MATH_HELPER = "\\\$[ \\t](.+?)(?<![ \\t])[ \\t]\\\$"
