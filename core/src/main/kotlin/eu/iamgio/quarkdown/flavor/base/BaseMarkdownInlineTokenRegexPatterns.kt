package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.InlineCodeToken
import eu.iamgio.quarkdown.lexer.InlineTextToken
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.PunctuationToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisLeftDelimeterToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisRightDelimeterAsteriskToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisRightDelimeterUnderscoreToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
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

    val punctuation
        get() =
            TokenRegexPattern(
                name = "InlinePunctuation",
                wrap = ::PunctuationToken,
                regex =
                    RegexBuilder("((?![*_])[\\spunct])")
                        .withReference("punct", PUNCTUATION_HELPER)
                        .build(),
            )

    /**
     * Sequences emphasis should skip over: `[title](link)`, `\`code\``, `<html>`
     */
    val blockSkip
        get() =
            TokenRegexPattern(
                name = "InlinePunctuation",
                wrap = ::PunctuationToken,
                regex =
                    RegexBuilder("\\[[^[\\]]*?\\]\\([^()]*?\\)|`[^`]*?`|<[^<>]*?>")
                        .build(),
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

    val strongEmphasisLeftDelimeter
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisLeftDelimeter",
                wrap = ::StrongEmphasisLeftDelimeterToken,
                regex =
                    RegexBuilder("(?:\\*+(?:((?!\\*)[punct])|[^\\s*]))|_+(?:((?!_)[punct])|([^\\s_]))")
                        .withReference("punct", PUNCTUATION_HELPER)
                        .build(),
            )

    val strongEmphasisRightDelimeterAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisRightDelimeterAsterisk",
                wrap = ::StrongEmphasisRightDelimeterAsteriskToken,
                regex =
                    RegexBuilder(STRONG_EMPHASIS_RIGHT_DELIMETER_ASTERISK_HELPER)
                        .withReference("punct", PUNCTUATION_HELPER)
                        .build(),
            )

    val strongEmphasisRightDelimeterUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisRightDelimeterUnderscore",
                wrap = ::StrongEmphasisRightDelimeterUnderscoreToken,
                regex =
                    RegexBuilder(STRONG_EMPHASIS_RIGHT_DELIMETER_UNDERSCORE_HELPER)
                        .withReference("punct", PUNCTUATION_HELPER)
                        .build().also { println(it) },
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

private const val PUNCTUATION_HELPER = "\\p{P}\\p{S}"

private const val STRONG_EMPHASIS_RIGHT_DELIMETER_ASTERISK_HELPER =
    "[^_*]*?__[^_*]*?\\*[^_*]*?(?=__)" + // Skip orphan inside strong
        "|[^*]+(?=[^*])" + // Consume to delim
        "|(?!\\*)[punct](\\*+)(?=[\\s]|$)" + // (1) #*** can only be a Right Delimiter
        "|[^punct\\s](\\*+)(?!\\*)(?=[punct\\s]|$)" + // (2) a***#, a*** can only be a Right Delimiter
        "|(?!\\*)[punct\\s](\\*+)(?=[^punct\\s])" + // (3) #***a, ***a can only be Left Delimiter
        "|[\\s](\\*+)(?!\\*)(?=[punct])" + // (4) ***# can only be Left Delimiter
        "|(?!\\*)[punct](\\*+)(?!\\*)(?=[punct])" + // (5) #***# can be either Left or Right Delimiter
        "|[^punct\\s](\\*+)(?=[^punct\\s])" // (6) a***a can be either Left or Right Delimiter

private const val STRONG_EMPHASIS_RIGHT_DELIMETER_UNDERSCORE_HELPER =
    "[^_*]*?\\*\\*[^_*]*?_[^_*]*?(?=\\*\\*)" + // Skip orphan inside strong
        "|[^_]+(?=[^_])" + // Consume to delim
        "|(?!_)[punct](_+)(?=[\\s]|$)" + // (1) #___ can only be a Right Delimiter
        "|[^punct\\s](_+)(?!_)(?=[punct\\s]|$)" + // (2) a___#, a___ can only be a Right Delimiter
        "|(?!_)[punct\\s](_+)(?=[^punct\\s])" + // (3) #___a, ___a can only be Left Delimiter
        "|[\\s](_+)(?!_)(?=[punct])" + // (4) ___# can only be Left Delimiter
        "|(?!_)[punct](_+)(?!_)(?=[punct])"
