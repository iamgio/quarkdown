package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.base.BaseMarkdownBlockTokenRegexPatterns
import eu.iamgio.quarkdown.lexer.MultilineMathToken
import eu.iamgio.quarkdown.lexer.OnelineMathToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * Regex patterns for [QuarkdownFlavor].
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
                    " {0,3}\\\$[ \\t](.+?)[ \\t]\\\$\\s*\$"
                        .toRegex(),
            )
}
