package eu.iamgio.quarkdown.lexer.patterns

import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import eu.iamgio.quarkdown.lexer.tokens.CodeSpanToken
import eu.iamgio.quarkdown.lexer.tokens.CommentToken
import eu.iamgio.quarkdown.lexer.tokens.CriticalContentToken
import eu.iamgio.quarkdown.lexer.tokens.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.tokens.EmphasisToken
import eu.iamgio.quarkdown.lexer.tokens.EntityToken
import eu.iamgio.quarkdown.lexer.tokens.EscapeToken
import eu.iamgio.quarkdown.lexer.tokens.ImageToken
import eu.iamgio.quarkdown.lexer.tokens.LineBreakToken
import eu.iamgio.quarkdown.lexer.tokens.LinkToken
import eu.iamgio.quarkdown.lexer.tokens.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.tokens.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.tokens.StrikethroughToken
import eu.iamgio.quarkdown.lexer.tokens.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.tokens.StrongToken
import eu.iamgio.quarkdown.lexer.tokens.UrlAutolinkToken

/**
 * Regex patterns for [eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor] inlines.
 */
open class BaseMarkdownInlineTokenRegexPatterns {
    /**
     * A backslash followed by a punctuation character.
     * @see EscapeToken
     */
    val escape
        get() =
            TokenRegexPattern(
                name = "InlineEscape",
                wrap = ::EscapeToken,
                regex =
                    "\\\\([!\"#\$%&'()*+,\\-./:;<=>?@\\[\\]\\\\^_`{|}~])"
                        .toRegex(),
            )

    /**
     * A text entity: `&#10`, `&xFF`, `&nbsp;`.
     * @see EntityToken
     */
    val entity
        get() =
            TokenRegexPattern(
                name = "InlineEntity",
                wrap = ::EntityToken,
                regex =
                    "&(#(\\d+)|#x([0-9A-Fa-f]+)|\\w+);?"
                        .toRegex(),
            )

    /**
     * Characters that require attention in the rendering stage.
     * @see CriticalContentToken
     */
    val criticalContent
        get() =
            TokenRegexPattern(
                name = "InlineCriticalContent",
                wrap = ::CriticalContentToken,
                regex =
                    "[&<>\"']"
                        .toRegex(),
            )

    /**
     * An inline fragment of code wrapped by sequences of backticks of the same length.
     * @see CodeSpanToken
     */
    val codeSpan
        get() =
            TokenRegexPattern(
                name = "InlineCodeSpan",
                wrap = ::CodeSpanToken,
                regex =
                    "(?<!`)(?<codebegin>`+)([^`]|[^`][\\s\\S]*?[^`])\\k<codebegin>(?!`)"
                        .toRegex(),
            )

    /**
     * A hard line break given by two or more spaces at the end of the line.
     * @see LineBreakToken
     */
    val lineBreak
        get() =
            TokenRegexPattern(
                name = "InlineLineBreak",
                wrap = ::LineBreakToken,
                regex =
                    "( {2,}|\\\\)\\R(?!\\s*\$)"
                        .toRegex(),
            )

    /**
     * A link with its label in square brackets and its URL and optional title in parentheses,
     * without spaces in-between.
     * @see LinkToken
     */
    val link
        get() =
            TokenRegexPattern(
                name = "InlineLink",
                wrap = ::LinkToken,
                regex =
                    RegexBuilder("\\[(label)\\]\\(\\s*(href)(?:\\s+(title))?\\s*\\)")
                        .withReference("label", LABEL_HELPER)
                        .withReference("href", "<(?:\\\\.|[^\\n<>\\\\])+>|[^\\s\\x00-\\x1f]*")
                        .withReference("title", DELIMITED_TITLE_HELPER)
                        .build(),
            )

    /**
     * A URL wrapped in angle brackets.
     * @see DiamondAutolinkToken
     */
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

    /**
     * A plain URL.
     * @see UrlAutolinkToken
     */
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

    /**
     * A link whose reference in brackets matches that of a link definition,
     * with an optional label in brackets at the beginning.
     * @see ReferenceLinkToken
     */
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

    /**
     * An image, same as a link preceded by a `!`.
     * As an extension, Quarkdown introduces an optional `(WxH)` to be added at the end which specifies
     * the image size, where W and H can be integers or `_` (auto).
     * @see ImageToken
     * @see link
     */
    val image
        get() =
            TokenRegexPattern(
                name = "InlineImage",
                wrap = ::ImageToken,
                regex =
                    RegexBuilder("!(?:\\(imgsize\\))?link")
                        .withReference("imgsize", "(?<imgwidth>\\d+|_)x(?<imgheight>\\d+|_)")
                        .withReference("link", link.regex.pattern)
                        .build(),
                groupNames = listOf("imgwidth", "imgheight"),
            )

    /**
     * An image that references a link definition, same as a reference link preceded by a `!`.
     * @see ReferenceImageToken
     * @see referenceLink
     */
    val referenceImage
        get() =
            TokenRegexPattern(
                name = "InlineReferenceImage",
                wrap = ::ReferenceImageToken,
                regex =
                    RegexBuilder("!(?:\\(imgsize\\))?link")
                        .withReference("imgsize", "(?<refimgwidth>\\d+|_)x(?<refimgheight>\\d+|_)")
                        .withReference("link", referenceLink.regex.pattern)
                        .build(),
                groupNames = listOf("refimgwidth", "refimgheight"),
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

    /**
     * An ignored piece of content wrapped in `<!-- ... -->` (the amount of `-` can vary).
     * @see CommentToken
     */
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

    /**
     * Content wrapped in single asterisks, following CommonMark emphasis guidelines.
     * @see EmphasisToken
     */
    val emphasisAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineEmphasisAsterisk",
                wrap = ::EmphasisToken,
                regex = delimiteredPattern(startDelimiter = "\\*", endDelimiter = "\\*+", strict = false),
            )

    /**
     * Content wrapped in single underscored, following CommonMark emphasis guidelines.
     * @see EmphasisToken
     */
    val emphasisUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineEmphasisUnderscore",
                wrap = ::EmphasisToken,
                regex = delimiteredPattern(startDelimiter = "_", endDelimiter = "_+", strict = true),
            )

    /**
     * Content wrapped in double asterisks, following CommonMark emphasis guidelines.
     * @see StrongToken
     */
    val strongAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineStrongAsterisk",
                wrap = ::StrongToken,
                regex = delimiteredPattern(startDelimiter = "\\*{2}", endDelimiter = "\\*{2,}", strict = false),
            )

    /**
     * Content wrapped in double underscores, following CommonMark emphasis guidelines.
     * @see StrongToken
     */
    val strongUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineStrongUnderscore",
                wrap = ::StrongToken,
                regex = delimiteredPattern(startDelimiter = "_{2}", endDelimiter = "_{2,}", strict = true),
            )

    /**
     * Content wrapped in triple asterisks, following CommonMark emphasis guidelines.
     * @see StrongEmphasisToken
     */
    val strongEmphasisAsterisk
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisAsterisk",
                wrap = ::StrongEmphasisToken,
                regex = delimiteredPattern(startDelimiter = "\\*{3}", endDelimiter = "\\*{3,}", strict = false),
            )

    /**
     * Content wrapped in triple underscores, following CommonMark emphasis guidelines.
     * @see StrongEmphasisToken
     */
    val strongEmphasisUnderscore
        get() =
            TokenRegexPattern(
                name = "InlineStrongEmphasisUnderscore",
                wrap = ::StrongEmphasisToken,
                regex = delimiteredPattern(startDelimiter = "_{3}", endDelimiter = "_{3,}", strict = true),
            )

    /**
     * Content wrapped in double tildes, following CommonMark emphasis guidelines.
     * @see StrikethroughToken
     */
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

// "This is a title", 'This is a title', (This is a title)
internal const val DELIMITED_TITLE_HELPER =
    "\"(?:\\\\\"?|[^\"\\\\])*\"|'(?:\\\\'?|[^'\\\\])*'|\\((?:\\\\\\)?|[^)\\\\])*\\)"

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
        "(?!start)((.|\\R)+?)" +
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
