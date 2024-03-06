package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.InlineCodeToken
import eu.iamgio.quarkdown.lexer.InlineTextToken
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 *
 */
class BaseMarkdownInlineTokenRegexPatterns {
    val escape
        get() =
            TokenRegexPattern(
                name = "InlineEscape",
                wrap = ::EscapeToken,
                regex =
                    "\\\\([!\"#\$%&'()*+,\\-./:;<=>?@\\[\\]\\\\^_`{|}~])"
                        .toRegex(),
            )

    val code
        get() =
            TokenRegexPattern(
                name = "InlineCode",
                wrap = ::InlineCodeToken,
                regex =
                    "(?<codebegin>`+)([^`]|[^`][\\s\\S]*?[^`])\\k<codebegin>(?!`)"
                        .toRegex(),
            )

    val lineBreak
        get() =
            TokenRegexPattern(
                name = "InlineLineBreak",
                wrap = ::LineBreakToken,
                regex =
                    "( {2,}|\\\\)\\n(?!\\s*\$)"
                        .toRegex(),
            )

    val text
        get() =
            TokenRegexPattern(
                name = "InlineText",
                wrap = ::InlineTextToken,
                regex =
                    "(`+|[^`])(?:(?= {2,}\\n)|[\\s\\S]*?(?:(?=[\\\\<!\\[`*_]|\\b_|\$)|[^ ](?= {2,}\\n)))"
                        .toRegex(),
            )
}
