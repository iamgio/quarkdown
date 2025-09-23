package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.patterns.PatternHelpers.customId
import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.tokens.BlockCodeToken
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import com.quarkdown.core.lexer.tokens.BlockTextToken
import com.quarkdown.core.lexer.tokens.CommentToken
import com.quarkdown.core.lexer.tokens.FencesCodeToken
import com.quarkdown.core.lexer.tokens.FootnoteDefinitionToken
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
            .withReference("fences", "^ {0,3}((`{3,})|(~{3,}))[^\\n]*\\n")
            .withReference("blockquote", " {0,3}>")
            .apply {
                if (includeList) withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
                if (includeTable) withReference("table", table.regex.pattern)
            }.build()

    /**
     * 4-spaces indented content.
     * @see BlockCodeToken
     */
    val blockCode by lazy {
        TokenRegexPattern(
            name = "BlockCode",
            wrap = ::BlockCodeToken,
            regex =
                "^( {4}[^\\n]+(?:\\n(?: *(?:\\n|\$))*)?)+"
                    .toRegex(),
        )
    }

    /**
     * `>`-beginning content.
     * @see BlockQuoteToken
     */
    val blockQuote by lazy {
        TokenRegexPattern(
            name = "BlockQuote",
            wrap = ::BlockQuoteToken,
            regex =
                RegexBuilder("^( {0,3}> ?(paragraph|[^\\n]*)(?:\\n|$))+")
                    .withReference("paragraph", paragraph.regex.pattern)
                    .build(),
        )
    }

    /**
     * Any previously unmatched content (should not happen).
     * @see BlockTextToken
     */
    val blockText by lazy {
        TokenRegexPattern(
            name = "BlockText",
            wrap = ::BlockTextToken,
            regex =
                "^[^\\n]+"
                    .toRegex(),
        )
    }

    /**
     * An ignored piece of content wrapped in `<!-- ... -->` (the amount of `-` can vary).
     * @see CommentToken
     */
    val comment by lazy {
        TokenRegexPattern(
            name = "BlockComment",
            wrap = ::CommentToken,
            regex = PatternHelpers.COMMENT,
        )
    }

    /**
     * Fenced content within triple backticks or tildes, with an optional language tag.
     * @see FencesCodeToken
     */
    val fencesCode by lazy {
        TokenRegexPattern(
            name = "FencesCode",
            wrap = ::FencesCodeToken,
            regex =
                RegexBuilder("^ {0,3}fencesstart[ \\t]*lang?$(?s)(.+?)fencesend[ \\t]*$")
                    .withReference("fencesstart", "(?<fenceschar>[`~]){3,}")
                    .withReference("fencesend", "\\k<fenceschar>{3,}")
                    .withReference("lang", "(?<fencescodelang>.+?)")
                    .build(),
            groupNames = listOf("fenceschar", "fencescodelang"),
        )
    }

    /**
     * Content that begins by a variable amount of `#`s.
     * @see HeadingToken
     */
    val heading by lazy {
        TokenRegexPattern(
            name = "Heading",
            wrap = ::HeadingToken,
            regex =
                RegexBuilder("^ {0,3}(#{1,6})(!?)(?=\\s|$)(.*?)customid?trailing(?:\\n+|$)")
                    .withReference("customid", customId("heading"))
                    .withReference("trailing", "\\s*#*") // Trailing #s are ignored
                    .build(),
            groupNames = listOf("headingcustomid"),
        )
    }

    /**
     * Three or more bullets in sequence.
     * @see HorizontalRuleToken
     */
    val horizontalRule by lazy {
        TokenRegexPattern(
            name = "HorizontalRule",
            wrap = ::HorizontalRuleToken,
            regex =
                "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\R+|$)"
                    .toRegex(),
        )
    }

    /**
     * Pattern builder for link definitions and footnote definitions.
     * @param inBrackets pattern for the label contained within the brackets
     * @param interruption pattern for the interruption rule of the definition.
     * For instance, link definitions are one-liners, while footnote definitions can be multiline
     * and interrupted the same way as paragraphs.
     */
    private fun definitionPattern(
        inBrackets: String,
        content: String,
        interruption: String,
    ): Regex =
        RegexBuilder("^ {0,3}\\[$inBrackets\\]: *(?:\\n *)?$content *$interruption")
            .withReference("label", "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+")
            .build()

    /**
     * Creation of a referenceable link defined by label, url and optional title.
     * @see LinkDefinitionToken
     */
    val linkDefinition by lazy {
        TokenRegexPattern(
            name = "LinkDefinition",
            wrap = ::LinkDefinitionToken,
            regex =
                definitionPattern(
                    inBrackets = "(label)",
                    content =
                        RegexBuilder("([^<\\s][^\\s]*|<.*?>)(?:(?: +(?:\\n *)?| *\\n *)(title))?")
                            .withReference(
                                "title",
                                "(?:\"(?:\\\\\"?|[^\"\\\\])*\"|'[^'\\n]*(?:\\n[^'\\n]+)*\\n?'|\\([^()]*\\))",
                            ).build()
                            .pattern,
                    interruption = "(?:\\n+|$)",
                ),
        )
    }

    /**
     * Creation of a referenceable footnote defined by label and content.
     * @see FootnoteDefinitionToken
     */
    val footnoteDefinition by lazy {
        TokenRegexPattern(
            name = "FootnoteDefinition",
            wrap = ::FootnoteDefinitionToken,
            regex =
                definitionPattern(
                    inBrackets = "\\^(label)",
                    content = "",
                    interruption = "(.+(?:\\n(?!${interruptionRule()})[^\\n]+)*)*",
                ),
        )
    }

    /**
     * Item of a list.
     * @see ListItemToken
     */
    val listItem by lazy {
        TokenRegexPattern(
            name = "ListItem",
            wrap = ::ListItemToken,
            regex =
                RegexBuilder(
                    "^(( {0,3})(?:bullet))([ \\t]\\[[ xX]\\]|(?:))[ \\t](((.+(\\n(?!(\\s+\\n| {0,3}(bullet))))?)*(\\s*^\\3 {2,})*)*)",
                ).withReference("bullet", PatternHelpers.BULLET)
                    .build(),
        )
    }

    /**
     * A blank line.
     * @see NewlineToken
     */
    val newline by lazy {
        TokenRegexPattern(
            name = "Newline",
            wrap = ::NewlineToken,
            regex =
                "^(?: *(?:\\n|$))+"
                    .toRegex(),
        )
    }

    /**
     * A numbered list.
     * @see OrderedListToken
     */
    val orderedList by lazy {
        TokenRegexPattern(
            name = "OrderedList",
            wrap = ::OrderedListToken,
            regex =
                listPattern(
                    bulletInitialization = "\\d{1,9}(?<orderedbull>[\\.)])",
                    bulletContinuation = "\\d{1,9}\\k<orderedbull>",
                ),
        )
    }

    /**
     * Plain text content.
     * @see ParagraphToken
     */
    val paragraph by lazy {
        TokenRegexPattern(
            name = "Paragraph",
            wrap = ::ParagraphToken,
            regex =
                RegexBuilder("([^\\n]+(?:\\n(?!interruption)[^\\n]+)*)")
                    .withReference("interruption", interruptionRule().pattern)
                    .build(),
        )
    }

    /**
     * Text followed by a horizontal rule on a new line.
     * @see SetextHeadingToken
     */
    val setextHeading by lazy {
        TokenRegexPattern(
            name = "SetextHeading",
            wrap = ::SetextHeadingToken,
            regex =
                RegexBuilder("^(?:(?:(?! {0,3}(?:bullet))(.+?)customid?\\R)+?)bar *(?:\\R+|$)")
                    .withReference("bullet", PatternHelpers.BULLET)
                    .withReference("bar", " {0,3}(=+|-+)")
                    .withReference("customid", customId("setext"))
                    .build(),
            groupNames = listOf("setextcustomid"),
        )
    }

    /**
     * GFM table with a header row, a delimiter row and multiple cell rows.
     * @see TableToken
     */
    val table by lazy {
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
    }

    /**
     * A non-numbered list defined by the same kind of bullets.
     * @see UnorderedListToken
     */
    val unorderedList by lazy {
        TokenRegexPattern(
            name = "UnorderedList",
            wrap = ::UnorderedListToken,
            regex =
                listPattern(
                    bulletInitialization = "(?<unorderedbull>[*+-])",
                    bulletContinuation = "\\k<unorderedbull>",
                ),
        )
    }

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
