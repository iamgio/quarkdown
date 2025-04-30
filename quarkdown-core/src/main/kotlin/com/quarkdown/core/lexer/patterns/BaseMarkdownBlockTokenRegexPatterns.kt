package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.BlockCodeToken
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import com.quarkdown.core.lexer.tokens.BlockTextToken
import com.quarkdown.core.lexer.tokens.CommentToken
import com.quarkdown.core.lexer.tokens.FencesCodeToken
import com.quarkdown.core.lexer.tokens.HeadingToken
import com.quarkdown.core.lexer.tokens.HorizontalRuleToken
import com.quarkdown.core.lexer.tokens.LinkDefinitionToken
import com.quarkdown.core.lexer.tokens.ListItemToken
import com.quarkdown.core.lexer.tokens.NewlineToken
import com.quarkdown.core.lexer.tokens.OrderedListToken
import com.quarkdown.core.lexer.tokens.ParagraphToken
import com.quarkdown.core.lexer.tokens.SetextHeadingToken
import com.quarkdown.core.lexer.tokens.TableToken
import com.quarkdown.core.lexer.tokens.UnorderedListToken

/**
 * Regex patterns for [com.quarkdown.core.flavor.base.BaseMarkdownFlavor] blocks.
 */
open class BaseMarkdownBlockTokenRegexPatterns {
    /**
     * The rules that defines when a text node must interrupt.
     * This might be overridden by subclasses to add new interruptions.
     * @param includeList whether the `list` reference should be filled
     * @param includeTable whether the `table` reference should be filled
     */
    open fun interruptionRule(
        includeList: Boolean = true,
        includeTable: Boolean = true,
    ): Regex =
        RegexBuilder("hr|heading|blockquote|fences|list|html|table| +\\n")
            .withReference("hr", horizontalRule.regex.pattern) // Interrupts on horizontal rule
            .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
            .withReference("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n")
            .withReference("blockquote", " {0,3}>")
            .apply {
                if (includeList) withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
                if (includeTable) withReference("table", table.regex.pattern)
            }.build()

    /**
     * 4-spaces indented content.
     * @see BlockCodeToken
     */
    val blockCode
        get() =
            TokenRegexPattern(
                name = "BlockCode",
                wrap = ::BlockCodeToken,
                regex =
                    "^( {4}[^\\n]+(?:\\n(?: *(?:\\n|\$))*)?)+"
                        .toRegex(),
            )

    /**
     * `>`-beginning content.
     * @see BlockQuoteToken
     */
    val blockQuote
        get() =
            TokenRegexPattern(
                name = "BlockQuote",
                wrap = ::BlockQuoteToken,
                regex =
                    RegexBuilder("^( {0,3}> ?(paragraph|[^\\n]*)(?:\\n|$))+")
                        .withReference("paragraph", paragraph.regex.pattern)
                        .build(),
            )

    /**
     * Any previously unmatched content (should not happen).
     * @see BlockTextToken
     */
    val blockText =
        TokenRegexPattern(
            name = "BlockText",
            wrap = ::BlockTextToken,
            regex =
                "^[^\\n]+"
                    .toRegex(),
        )

    /**
     * An ignored piece of content wrapped in `<!-- ... -->` (the amount of `-` can vary).
     * @see CommentToken
     */
    val comment =
        TokenRegexPattern(
            name = "BlockComment",
            wrap = ::CommentToken,
            regex = COMMENT_PATTERN,
        )

    /**
     * Fenced content within triple backticks or tildes, with an optional language tag.
     * @see FencesCodeToken
     */
    val fencesCode
        get() =
            TokenRegexPattern(
                name = "FencesCode",
                wrap = ::FencesCodeToken,
                regex =
                    "^ {0,3}((?<fenceschar>[`~]){3,})($|\\s*.+$)((.|\\s)+?)(\\k<fenceschar>{3,})"
                        .toRegex(),
            )

    /**
     * Content that begins by a variable amount of `#`s.
     * @see HeadingToken
     */
    val heading
        get() =
            TokenRegexPattern(
                name = "Heading",
                wrap = ::HeadingToken,
                regex =
                    "^ {0,3}(#{1,6})(!?)(?=\\s|$)(.*)(?:\\n+|$)"
                        .toRegex(),
            )

    /**
     * Three or more bullets in sequence.
     * @see HorizontalRuleToken
     */
    val horizontalRule
        get() =
            TokenRegexPattern(
                name = "HorizontalRule",
                wrap = ::HorizontalRuleToken,
                regex =
                    "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\R+|$)"
                        .toRegex(),
            )

    /**
     * Creation of a link reference defined by label, url and optional title.
     * @see LinkDefinitionToken
     */
    val linkDefinition
        get() =
            TokenRegexPattern(
                name = "LinkDefinition",
                wrap = ::LinkDefinitionToken,
                regex =
                    RegexBuilder("^ {0,3}\\[(label)\\]: *(?:\\n *)?([^<\\s][^\\s]*|<.*?>)(?:(?: +(?:\\n *)?| *\\n *)(title))? *(?:\\n+|$)")
                        .withReference("label", "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+")
                        .withReference(
                            "title",
                            "(?:\"(?:\\\\\"?|[^\"\\\\])*\"|'[^'\\n]*(?:\\n[^'\\n]+)*\\n?'|\\([^()]*\\))",
                        ).build(),
            )

    /**
     * Item of a list.
     * @see ListItemToken
     */
    val listItem
        get() =
            TokenRegexPattern(
                name = "ListItem",
                wrap = ::ListItemToken,
                regex =
                    RegexBuilder(
                        "^(( {0,3})(?:bullet))([ \\t]\\[[ xX]\\]|(?:))[ \\t](((.+(\\n(?!(\\s+\\n| {0,3}(bullet))))?)*(\\s*^\\3 {2,})*)*)",
                    ).withReference("bullet", BULLET_HELPER)
                        .build(),
            )

    /**
     * A blank line.
     * @see NewlineToken
     */
    val newline =
        TokenRegexPattern(
            name = "Newline",
            wrap = ::NewlineToken,
            regex =
                "^(?: *(?:\\n|$))+"
                    .toRegex(),
        )

    /**
     * A numbered list.
     * @see OrderedListToken
     */
    val orderedList
        get() =
            TokenRegexPattern(
                name = "OrderedList",
                wrap = ::OrderedListToken,
                regex =
                    listPattern(
                        bulletInitialization = "\\d{1,9}(?<orderedbull>[\\.)])",
                        bulletContinuation = "\\d{1,9}\\k<orderedbull>",
                    ),
            )

    /**
     * Plain text content.
     * @see ParagraphToken
     */
    val paragraph
        get() =
            TokenRegexPattern(
                name = "Paragraph",
                wrap = ::ParagraphToken,
                regex =
                    RegexBuilder("([^\\n]+(?:\\n(?!interruption)[^\\n]+)*)")
                        .withReference("interruption", interruptionRule().pattern)
                        .build(),
            )

    /**
     * Text followed by a horizontal rule on a new line.
     * @see SetextHeadingToken
     */
    val setextHeading
        get() =
            TokenRegexPattern(
                name = "SetextHeading",
                wrap = ::SetextHeadingToken,
                regex =
                    RegexBuilder("^(?!bullet )((?:.|\\R(?!\\s*?\\R|bullet ))+?)\\R {0,3}(=+|-+) *(?:\\R+|$)")
                        .withReference("bullet", BULLET_HELPER)
                        .build(),
            )

    /**
     * GFM table with a header row, a delimiter row and multiple cell rows.
     * @see TableToken
     */
    val table
        get() =
            TokenRegexPattern(
                name = "Table",
                wrap = ::TableToken,
                regex =
                    RegexBuilder(
                        // Header
                        "^ *([^\\n ].*)\\n" +
                            // Align
                            " {0,3}((?:\\| *)?:?-+:? *(?:\\| *:?-+:? *)*(?:\\| *)?)" +
                            // Cells
                            "(?:\\n((?:(?! *\\n|interruption).*(?:\\n|$))*)\\n*|$)",
                    ).withReference("interruption", interruptionRule(includeTable = false).pattern)
                        .withReference("|table", "")
                        .build(),
            )

    /**
     * A non-numbered list defined by the same kind of bullets.
     * @see UnorderedListToken
     */
    val unorderedList
        get() =
            TokenRegexPattern(
                name = "UnorderedList",
                wrap = ::UnorderedListToken,
                regex =
                    listPattern(
                        bulletInitialization = "(?<unorderedbull>[*+-])",
                        bulletContinuation = "\\k<unorderedbull>",
                    ),
            )

    /**
     * Generates a regex TokenRegexPattern that matches a whole list block.
     * @param bulletInitialization bullet TokenRegexPattern that begins the block
     * @param bulletContinuation bullet TokenRegexPattern that continues the block (all the items should ideally share the same bullet type)
     */
    private fun listPattern(
        bulletInitialization: String,
        bulletContinuation: String,
    ): Regex {
        val initialization = "^(( {0,3}$bulletInitialization)[ \\t]((?!^(\\s*\\n){2})"
        val continuation = "(.+(\\n|\$)|\\n\\s*^( {2,}| {0,3}$bulletContinuation[ \\t]))"

        return RegexBuilder("$initialization$continuation(?!^(interruption)))*)+")
            .withReference("interruption", interruptionRule(includeList = false).pattern)
            .withReference("list", " {0,3}(?:[*+-]|\\d[.)]) ")
            .build()
    }
}

// Helper expressions

private const val BULLET_HELPER = "[*+-]|\\d{1,9}[\\.)]"

internal val COMMENT_PATTERN = "<!--(-?>|[\\s\\S]*?-->)".toRegex()
