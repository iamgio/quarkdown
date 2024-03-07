@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.lexer.*
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

    val anyPunctuation
        get() =
            TokenRegexPattern(
                name = "InlineAnyPunctuation",
                wrap = ::AnyPunctuationToken,
                regex =
                    RegexBuilder("\\\\([punct])")
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
                        .build(),
            )

    val link
        get() =
            TokenRegexPattern(
                name = "InlineLink",
                wrap = ::LinkToken,
                regex =
                    RegexBuilder("!?\\[(label)\\]\\(\\s*(href)(?:\\s+(title))?\\s*\\)")
                        .withReference("label", LABEL_HELPER)
                        .withReference("href", "<(?:\\\\.|[^\\n<>\\\\])+>|[^\\s\\x00-\\x1f]*")
                        .withReference(
                            "title",
                            "\"(?:\\\\\"?|[^\"\\\\])*\"|'(?:\\\\'?|[^'\\\\])*'|\\((?:\\\\\\)?|[^)\\\\])*\\)",
                        )
                        .build(),
            )

    val referenceLink
        get() =
            TokenRegexPattern(
                name = "InlineReferenceLink",
                wrap = ::ReferenceLinkToken,
                regex =
                    RegexBuilder("!?\\[(label)\\]\\[(ref)\\]")
                        .withReference("label", LABEL_HELPER)
                        .withReference("ref", BLOCK_LABEL_HELPER)
                        .build(),
            )

    val collapsedReferenceLink
        get() =
            TokenRegexPattern(
                name = "InlineCollapsedReferenceLink",
                wrap = ::CollapsedReferenceLinkToken,
                regex =
                    RegexBuilder("!?\\[(ref)\\](?:\\[\\])?")
                        .withReference("ref", BLOCK_LABEL_HELPER)
                        .build(),
            )

    val referenceLinkSearch
        get() =
            TokenRegexPattern(
                name = "InlineReferenceLinkSearch",
                wrap = ::ReferenceLinkSearchToken,
                regex =
                    RegexBuilder("reflink|nolink(?!\\()")
                        .withReference("reflink", referenceLink.regex.pattern)
                        .withReference("nolink", collapsedReferenceLink.regex.pattern)
                        .build(),
            )

    val autolink
        get() =
            TokenRegexPattern(
                name = "InlineAutolink",
                wrap = ::AutolinkToken,
                regex =
                    RegexBuilder("<(scheme:[^\\s\\x00-\\x1f<>]*|email)>")
                        .withReference("scheme", "[a-zA-Z][a-zA-Z0-9+.-]{1,31}")
                        .withReference(
                            "email",
                            "[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+(@)[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" +
                                "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+(?![-_])",
                        )
                        .build(),
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

    val comment
        get() =
            TokenRegexPattern(
                name = "InlineComment",
                wrap = ::CommentToken,
                regex =
                    RegexBuilder(COMMENT_TAG_HELPER)
                        .withReference("comment", COMMENT_HELPER)
                        .withReference(
                            "attribute",
                            "\\s+[a-zA-Z:_][\\w.:-]*(?:\\s*=\\s*\"[^\"]*\"|\\s*=\\s*'[^']*'|\\s*=\\s*[^\\s\"'=<>`]+)?",
                        )
                        .build(),
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

// [this is a label]
private const val LABEL_HELPER = "(?:\\[(?:\\\\.|[^\\[\\]\\\\])*\\]|\\\\.|`[^`]*`|[^\\[\\]\\\\`])*?"

private const val BLOCK_LABEL_HELPER = "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+"

private const val COMMENT_TAG_HELPER =
    "comment" +
        "|^</[a-zA-Z][\\w:-]*\\s*>" + // self-closing tag
        "|^<[a-zA-Z][\\w-]*(?:attribute)*?\\s*/?>" + // open tag
        "|^<\\?[\\s\\S]*?\\?>" + // processing instruction, e.g. <?php ?>
        "|^<![a-zA-Z]+\\s[\\s\\S]*?>" + // declaration, e.g. <!DOCTYPE html>
        "|^<!\\[CDATA\\[[\\s\\S]*?\\]\\]>" // CDATA section
