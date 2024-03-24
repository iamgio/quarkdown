@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.CodeSpanToken
import eu.iamgio.quarkdown.lexer.CommentToken
import eu.iamgio.quarkdown.lexer.CriticalContentToken
import eu.iamgio.quarkdown.lexer.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.EntityToken
import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.ImageToken
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.LinkToken
import eu.iamgio.quarkdown.lexer.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.StrikethroughToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.UrlAutolinkToken
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

    val entity
        get() =
            TokenRegexPattern(
                name = "InlineEntity",
                wrap = ::EntityToken,
                regex =
                    "&(#(\\d+)|#x([0-9A-Fa-f]+)|\\w+);?"
                        .toRegex(),
            )

    val criticalContent
        get() =
            TokenRegexPattern(
                name = "InlineCriticalContent",
                wrap = ::CriticalContentToken,
                regex =
                    "[&<>\"']"
                        .toRegex(),
            )

    val codeSpan
        get() =
            TokenRegexPattern(
                name = "InlineCodeSpan",
                wrap = ::CodeSpanToken,
                regex =
                    "(?<!`)(?<codebegin>`+)([^`]|[^`][\\s\\S]*?[^`])\\k<codebegin>(?!`)"
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

    val link
        get() =
            TokenRegexPattern(
                name = "InlineLink",
                wrap = ::LinkToken,
                regex =
                    RegexBuilder("\\[(label)\\]\\(\\s*(href)(?:\\s+(title))?\\s*\\)")
                        .withReference("label", LABEL_HELPER)
                        .withReference("href", "<(?:\\\\.|[^\\n<>\\\\])+>|[^\\s\\x00-\\x1f]*")
                        .withReference(
                            "title",
                            "\"(?:\\\\\"?|[^\"\\\\])*\"|'(?:\\\\'?|[^'\\\\])*'|\\((?:\\\\\\)?|[^)\\\\])*\\)",
                        )
                        .build(),
            )

    val diamondAutolink
        get() =
            TokenRegexPattern(
                name = "InlineDiamondAutolink",
                wrap = ::DiamondAutolinkToken,
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

    val urlAutolink
        get() =
            TokenRegexPattern(
                name = "InlineUrlAutolink",
                wrap = ::UrlAutolinkToken,
                regex =
                    RegexBuilder("url|email")
                        .withReference("url", "((?:ftp|https?):\\/\\/|www\\.)(?:[a-zA-Z0-9\\-]+\\.?)+[^\\s<]*")
                        .withReference(
                            "email",
                            "[A-Za-z0-9._+-]+(@)[a-zA-Z0-9-_]+(?:\\.[a-zA-Z0-9-_]*[a-zA-Z0-9])+(?![-_])",
                        )
                        .build(),
            )

    val referenceLink
        get() =
            TokenRegexPattern(
                name = "InlineReferenceLink",
                wrap = ::ReferenceLinkToken,
                regex =
                    RegexBuilder("\\[(label)\\](?:\\[(ref)?\\])?")
                        .withReference("label", LABEL_HELPER)
                        .withReference("ref", BLOCK_LABEL_HELPER)
                        .build(),
            )

    val image
        get() =
            TokenRegexPattern(
                name = "InlineImage",
                wrap = ::ImageToken,
                regex =
                    RegexBuilder("!link")
                        .withReference("link", link.regex.pattern)
                        .build(),
            )

    val referenceImage
        get() =
            TokenRegexPattern(
                name = "InlineReferenceImage",
                wrap = ::ReferenceImageToken,
                regex =
                    RegexBuilder("!link")
                        .withReference("link", referenceLink.regex.pattern)
                        .build(),
            )

    /*val text
        get() =
            TokenRegexPattern(
                name = "InlineText",
                wrap = ::InlineTextToken,
                regex =
                    "(`+|[^`])(?:(?= {2,}\\n)|[\\s\\S]*?(?:(?=[\\\\<!\\[`]|\\b_|\$)|[^ ](?= {2,}\\n)))"
                        .toRegex(),
            )*/

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

    // https://spec.commonmark.org/0.31.2/#emphasis-and-strong-emphasis

    val emphasisAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineEmphasisAsterisk",
                wrap = ::EmphasisToken,
                regex = delimiteredPattern(startDelimiter = "\\*", endDelimiter = "\\*+", strict = false),
            )

    val emphasisUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineEmphasisUnderscore",
                wrap = ::EmphasisToken,
                regex = delimiteredPattern(startDelimiter = "_", endDelimiter = "_+", strict = true),
            )

    val strongAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineStrongAsterisk",
                wrap = ::StrongToken,
                regex = delimiteredPattern(startDelimiter = "\\*{2}", endDelimiter = "\\*{2,}", strict = false),
            )

    val strongUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineStrongUnderscore",
                wrap = ::StrongToken,
                regex = delimiteredPattern(startDelimiter = "_{2}", endDelimiter = "_{2,}", strict = true),
            )

    val strongEmphasisAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisAsterisk",
                wrap = ::StrongEmphasisToken,
                regex = delimiteredPattern(startDelimiter = "\\*{3}", endDelimiter = "\\*{3,}", strict = false),
            )

    val strongEmphasisUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisUnderscore",
                wrap = ::StrongEmphasisToken,
                regex = delimiteredPattern(startDelimiter = "_{3}", endDelimiter = "_{3,}", strict = true),
            )

    val strikethrough
        get() =
            TokenRegexPattern(
                name = "InlineStrikethrough",
                wrap = ::StrikethroughToken,
                regex = delimiteredPattern("~{2}", strict = false),
            )
}

private const val PUNCTUATION_HELPER = "\\p{P}\\p{S}"

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

/**
 * @param startDelimiter begin of the match (included)
 * @param endDelimiter end of the match (included)
 * @param strict if `true`, restrictions are applied to the delimeter checks.
 * According to the CommonMark spec:
 * - non-strict means the start delimeter must be left-flanking and end delimeter must be right-flanking
 * - strict means any of the delimeters must not be left and right-flanking at the same time
 *
 * @return a new regex that matches the content between [startDelimiter] and [endDelimiter]
 */
private fun delimiteredPattern(
    startDelimiter: String,
    endDelimiter: String = startDelimiter,
    strict: Boolean,
) = RegexBuilder(
    "(?<!start)" +
        // Start delimiter
        if (strict) {
            // If strict, the start delimiter must also not be right-flanking
            "(?<=^|[\\spunct])(start(?!\\s))"
        } else {
            "(start(?![\\spunct])|(?<=^|[\\spunct])start(?!\\s))"
        } +
        // Content
        "(?!start)((.|\n)+?)" +
        // End delimiter
        if (strict) {
            // If strict, the start delimiter must also not be left-flanking
            "((?<![\\spunct])end(?=[\\spunct]))"
        } else {
            "((?<![\\spunct])end|(?<!\\s)end(?=[\\spunct]))"
        },
)
    .withReference("punct", PUNCTUATION_HELPER)
    .withReference("start", startDelimiter)
    .withReference("end", endDelimiter)
    .build()