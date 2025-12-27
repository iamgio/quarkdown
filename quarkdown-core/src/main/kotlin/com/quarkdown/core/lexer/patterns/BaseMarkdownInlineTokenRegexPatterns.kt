package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.CodeSpanToken
import com.quarkdown.core.lexer.tokens.CommentToken
import com.quarkdown.core.lexer.tokens.CriticalContentToken
import com.quarkdown.core.lexer.tokens.DiamondAutolinkToken
import com.quarkdown.core.lexer.tokens.EmphasisToken
import com.quarkdown.core.lexer.tokens.EntityToken
import com.quarkdown.core.lexer.tokens.EscapeToken
import com.quarkdown.core.lexer.tokens.ImageToken
import com.quarkdown.core.lexer.tokens.LineBreakToken
import com.quarkdown.core.lexer.tokens.LinkToken
import com.quarkdown.core.lexer.tokens.ReferenceFootnoteToken
import com.quarkdown.core.lexer.tokens.ReferenceImageToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken
import com.quarkdown.core.lexer.tokens.StrikethroughToken
import com.quarkdown.core.lexer.tokens.StrongEmphasisToken
import com.quarkdown.core.lexer.tokens.StrongToken
import com.quarkdown.core.lexer.tokens.UrlAutolinkToken

/**
 * Regex patterns for [com.quarkdown.core.flavor.base.BaseMarkdownFlavor] inlines.
 */
open class BaseMarkdownInlineTokenRegexPatterns {
    /**
     * A backslash followed by a punctuation character.
     * @see EscapeToken
     */
    val escape by lazy {
        TokenRegexPattern(
            name = "InlineEscape",
            wrap = ::EscapeToken,
            regex =
                "\\\\([!\"#$%&'()*+,\\-./:;<=>?@\\[\\]\\\\^_`{|}~])"
                    .toRegex(),
        )
    }

    /**
     * A text entity: `&#10`, `&xFF`, `&nbsp;`.
     * @see EntityToken
     */
    val entity by lazy {
        TokenRegexPattern(
            name = "InlineEntity",
            wrap = ::EntityToken,
            regex =
                "&(#(\\d+)|#x([0-9A-Fa-f]+)|\\w+);?"
                    .toRegex(),
        )
    }

    /**
     * Characters that require attention in the rendering stage.
     * @see CriticalContentToken
     */
    val criticalContent by lazy {
        TokenRegexPattern(
            name = "InlineCriticalContent",
            wrap = ::CriticalContentToken,
            regex =
                "[&<>\"']"
                    .toRegex(),
        )
    }

    /**
     * An inline fragment of code wrapped by sequences of backticks of the same length.
     * @see CodeSpanToken
     */
    val codeSpan by lazy {
        TokenRegexPattern(
            name = "InlineCodeSpan",
            wrap = ::CodeSpanToken,
            regex =
                "(?<!`)(?<codebegin>`+)([^`]|[^`][\\s\\S]*?[^`])\\k<codebegin>(?!`)"
                    .toRegex(),
        )
    }

    /**
     * A hard line break given by two or more spaces at the end of the line.
     * @see LineBreakToken
     */
    val lineBreak by lazy {
        TokenRegexPattern(
            name = "InlineLineBreak",
            wrap = ::LineBreakToken,
            regex =
                "( {2,}|\\\\)\\R(?!\\s*$)"
                    .toRegex(),
        )
    }

    /**
     * A link with its label in square brackets and its URL and optional title in parentheses,
     * without spaces in-between.
     * @see LinkToken
     */
    val link by lazy {
        TokenRegexPattern(
            name = "InlineLink",
            wrap = ::LinkToken,
            regex =
                RegexBuilder("\\[(label)\\]\\(\\s*(href)(?:\\s+(title))?\\s*\\)")
                    .withReference("label", LABEL_HELPER)
                    .withReference("href", "<(?:\\\\.|[^\\n<>\\\\])+>|[^\\s\\x00-\\x1f]*")
                    .withReference("title", PatternHelpers.DELIMITED_TITLE)
                    .build(),
        )
    }

    /**
     * A URL wrapped in angle brackets.
     * @see DiamondAutolinkToken
     */
    val diamondAutolink by lazy {
        TokenRegexPattern(
            name = "InlineDiamondAutolink",
            wrap = ::DiamondAutolinkToken,
            regex =
                RegexBuilder("<(scheme:[^\\s\\x00-\\x1f<>]*|email)>")
                    .withReference("scheme", "[a-zA-Z][a-zA-Z0-9+.-]{1,31}")
                    .withReference(
                        "email",
                        "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+(@)[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?" +
                            "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+(?![-_])",
                    ).build(),
        )
    }

    /**
     * A plain URL.
     * @see UrlAutolinkToken
     */
    val urlAutolink by lazy {
        TokenRegexPattern(
            name = "InlineUrlAutolink",
            wrap = ::UrlAutolinkToken,
            regex =
                RegexBuilder("url|email")
                    .withReference("url", "((?:ftp|https?):\\/\\/|www\\.)(?:[a-zA-Z0-9\\-]+\\.?)+[^\\s<]*")
                    .withReference(
                        "email",
                        "[A-Za-z0-9._+-]+(@)[a-zA-Z0-9-_]+(?:\\.[a-zA-Z0-9-_]*[a-zA-Z0-9])+(?![-_])",
                    ).build(),
        )
    }

    /**
     * A link whose reference in brackets matches that of a link definition,
     * with an optional label in brackets at the beginning.
     * @see ReferenceLinkToken
     */
    val referenceLink by lazy {
        TokenRegexPattern(
            name = "InlineReferenceLink",
            wrap = ::ReferenceLinkToken,
            regex =
                RegexBuilder("\\[(label)\\](?:\\[(ref)?\\])?")
                    .withReference("label", LABEL_HELPER)
                    .withReference("ref", BLOCK_LABEL_HELPER)
                    .build(),
        )
    }

    /**
     * A reference whose label in brackets matches that of a footnote definition.
     * It may also contain an optional all-in-one definition.
     * @see ReferenceLinkToken
     */
    val referenceFootnote by lazy {
        TokenRegexPattern(
            name = "InlineReferenceFootnote",
            wrap = ::ReferenceFootnoteToken,
            regex =
                RegexBuilder("\\[\\^(label)definition\\]")
                    .withReference("label", LABEL_HELPER)
                    .withReference("definition", "(?::[ \\t]*([^\\[\\]\\\\`]+?))?")
                    .build(),
        )
    }

    /**
     * An image, same as a link preceded by a `!`.
     * As an extension, Quarkdown introduces an optional `(WxH)` or `(W) to be added after the `!` the end which specifies
     * the image size, where W and H can be integers or `_` (auto).
     * @see ImageToken
     * @see link
     */
    val image by lazy {
        TokenRegexPattern(
            name = "InlineImage",
            wrap = ::ImageToken,
            regex =
                RegexBuilder("!(?:\\(imgsize\\))?linkcustomid?")
                    .withReference("imgsize", "(?<imgwidth>.+?)(?:sizedivider(?<imgheight>.+?))?")
                    .withReference("sizedivider", IMAGE_SIZE_DIVIDER_HELPER)
                    .withReference("link", link.regex.pattern)
                    .withReference("customid", PatternHelpers.customId("img"))
                    .build(),
            groupNames = listOf("imgwidth", "imgheight", "imgcustomid"),
        )
    }

    /**
     * An image that references a link definition, same as a reference link preceded by a `!`.
     * @see ReferenceImageToken
     * @see referenceLink
     */
    val referenceImage by lazy {
        TokenRegexPattern(
            name = "InlineReferenceImage",
            wrap = ::ReferenceImageToken,
            regex =
                RegexBuilder("!(?:\\(imgsize\\))?linkcustomid?")
                    .withReference("imgsize", "(?<refimgwidth>.+?)(?:sizedivider(?<refimgheight>.+?))?")
                    .withReference("sizedivider", IMAGE_SIZE_DIVIDER_HELPER)
                    .withReference("link", referenceLink.regex.pattern)
                    .withReference("customid", PatternHelpers.customId("refimg"))
                    .build(),
            groupNames = listOf("refimgwidth", "refimgheight", "refimgcustomid"),
        )
    }

    /**
     * An ignored piece of content wrapped in `<!-- ... -->` (the amount of `-` can vary).
     * @see CommentToken
     */
    val comment by lazy {
        TokenRegexPattern(
            name = "InlineComment",
            wrap = ::CommentToken,
            regex = PatternHelpers.COMMENT,
        )
    }

    // https://spec.commonmark.org/0.31.2/#emphasis-and-strong-emphasis

    /**
     * Content wrapped in single asterisks, following CommonMark emphasis guidelines.
     * @see EmphasisToken
     */
    val emphasisAsterisk by lazy {
        TokenRegexPattern(
            name = "InlineEmphasisAsterisk",
            wrap = ::EmphasisToken,
            regex = delimiteredPattern(startDelimiter = "\\*", endDelimiter = "\\*+", strict = false),
        )
    }

    /**
     * Content wrapped in single underscored, following CommonMark emphasis guidelines.
     * @see EmphasisToken
     */
    val emphasisUnderscore by lazy {
        TokenRegexPattern(
            name = "InlineEmphasisUnderscore",
            wrap = ::EmphasisToken,
            regex = delimiteredPattern(startDelimiter = "_", endDelimiter = "_+", strict = true),
        )
    }

    /**
     * Content wrapped in double asterisks, following CommonMark emphasis guidelines.
     * @see StrongToken
     */
    val strongAsterisk by lazy {
        TokenRegexPattern(
            name = "InlineStrongAsterisk",
            wrap = ::StrongToken,
            regex = delimiteredPattern(startDelimiter = "\\*{2}", endDelimiter = "\\*{2,}", strict = false),
        )
    }

    /**
     * Content wrapped in double underscores, following CommonMark emphasis guidelines.
     * @see StrongToken
     */
    val strongUnderscore by lazy {
        TokenRegexPattern(
            name = "InlineStrongUnderscore",
            wrap = ::StrongToken,
            regex = delimiteredPattern(startDelimiter = "_{2}", endDelimiter = "_{2,}", strict = true),
        )
    }

    /**
     * Content wrapped in triple asterisks, following CommonMark emphasis guidelines.
     * @see StrongEmphasisToken
     */
    val strongEmphasisAsterisk by lazy {
        TokenRegexPattern(
            name = "InlineStrongEmphasisAsterisk",
            wrap = ::StrongEmphasisToken,
            regex = delimiteredPattern(startDelimiter = "\\*{3}", endDelimiter = "\\*{3,}", strict = false),
        )
    }

    /**
     * Content wrapped in triple underscores, following CommonMark emphasis guidelines.
     * @see StrongEmphasisToken
     */
    val strongEmphasisUnderscore by lazy {
        TokenRegexPattern(
            name = "InlineStrongEmphasisUnderscore",
            wrap = ::StrongEmphasisToken,
            regex = delimiteredPattern(startDelimiter = "_{3}", endDelimiter = "_{3,}", strict = true),
        )
    }

    /**
     * Content wrapped in double tildes, following CommonMark emphasis guidelines.
     * @see StrikethroughToken
     */
    val strikethrough by lazy {
        TokenRegexPattern(
            name = "InlineStrikethrough",
            wrap = ::StrikethroughToken,
            regex = delimiteredPattern("~{2}", strict = false),
        )
    }
}

private const val PUNCTUATION_HELPER = "\\p{P}\\p{S}"

// [this is a label]
private const val LABEL_HELPER = "(?:\\[(?:\\\\.|[^\\[\\]\\\\])*\\]|\\\\.|`[^`]*`|[^\\[\\]\\\\`])*?"

private const val BLOCK_LABEL_HELPER = "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+"

// Width and height separator in images.
private const val IMAGE_SIZE_DIVIDER_HELPER = "(?:[* \\t]|(?<![a-zA-Z])x)" // 1*1, 1cm*1cm, 1 1, 1cm 1cm, 1x1 but not 1cmx1cm

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
            // If strict, the end delimiter must also not be left-flanking
            "((?<![\\spunct])end(?=[\\spunct])|(?<!\\s)end(?=[\\spunct]))"
        } else {
            "((?<![\\spunct])end|(?<!\\s)end(?=[\\spunct]))"
        },
).withReference("punct", PUNCTUATION_HELPER)
    .withReference("start", startDelimiter)
    .withReference("end", endDelimiter)
    .build()
