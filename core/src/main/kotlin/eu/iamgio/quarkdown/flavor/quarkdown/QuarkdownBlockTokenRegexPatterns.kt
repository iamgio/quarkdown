package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.base.BaseBlockTokenRegexPatterns
import eu.iamgio.quarkdown.lexer.MultilineMathToken
import eu.iamgio.quarkdown.lexer.OnelineMathToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 *
 */
class QuarkdownBlockTokenRegexPatterns : BaseBlockTokenRegexPatterns() {
    val multilineMath
        get() =
            TokenRegexPattern(
                name = "MultilineMath",
                ::MultilineMathToken,
                regex =
                    "^ {0,3}(\\\${3,})((.|\\s)+?)(\\\${3,})"
                        .toRegex(),
            )

    val onelineMath
        get() =
            TokenRegexPattern(
                name = "OnelineMath",
                ::OnelineMathToken,
                regex =
                    " {0,3}\\\$[ \\t](.+?)[ \\t]\\\$\\s*\$"
                        .toRegex(),
            )

    override val interruptionRule =
        RegexBuilder("mmath|omath|" + super.interruptionRule.pattern)
            .withReference("mmath", " {0,3}(?:\\\${3,})[^\\n]*\\n")
            .withReference("omath", onelineMath.regex.pattern)
            .build()
}
