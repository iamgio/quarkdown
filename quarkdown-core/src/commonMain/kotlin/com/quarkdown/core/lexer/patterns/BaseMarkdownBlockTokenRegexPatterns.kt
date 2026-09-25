package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.patterns.PatternHelpers.END_OF_INPUT
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
        RegexBuilder("hr|heading|blockquote|fences|list|table| +\\n")
            .withReference("hr", horizontalRule.regex) // Interrupts on horizontal rule
            .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
            .withReference("fences", "^ {0,3}((`{3,})|(~{3,}))[^\\n]*\\n")
            .withReference("blockquote", " {0,3}>")
            .apply {
                if (includeList) withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
                if (includeTable) withReference("table", table.regex)
            }.buildRegex()

    /**
     * 4-spaces indented content: lines indented by at least 4 spaces, possibly separated by blank lines.
     * @see BlockCodeToken
     */
    val blockCode by lazy {
        TokenRegexPattern(
            name = "BlockCode",
            wrap = ::BlockCodeToken,
            regex =
                RegexBuilder(
                    "^( {4}(?=[^\\n]*\\S)[\\s\\S]*?(?:$END_OF_INPUT|\\n(?!(?:[ \\n]*\\n)?codeline)(?:[ \\n]*\\n)?(?: *$END_OF_INPUT)?))",
                ).withReference("codeline", " {4}(?=[^\\n]*\\S)")
                    .build(),
        )
    }

    /**
     * `>`-beginning content: one or more quote lines, each of which may be followed by lazy continuation lines
     * (non-empty lines that are not interruptions) when the quote line has content of its own.
     * @see BlockQuoteToken
     */
    val blockQuote by lazy {
        TokenRegexPattern(
            name = "BlockQuote",
            wrap = ::BlockQuoteToken,
            regex =
                RegexBuilder(
                    "^( {0,3}>[\\s\\S]*?(?:$END_OF_INPUT|\\n(?! {0,3}>)(?:(?<=^ {0,3}> ?\\n)|(?![^\\n])|(?=interruption))))",
                ).withReference("interruption", interruptionRule().pattern)
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
                "^[^\\n]+",
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
                RegexBuilder(
                    "^( {0,3})fencesstart[ \\t]*lang?[ \\t]*caption?[ \\t]*customid?$" +
                        "(?s)(.+?)" +
                        "fencesend[ \\t]*$",
                ).withReference("fencesstart", "(?<fenceschar>[`~]){3,}")
                    .withReference("fencesend", "\\k<fenceschar>{3,}")
                    .withReference("lang", "(?<fencescodelang>.+?)")
                    .withReference("caption", "(?<fencescodecaption>${PatternHelpers.DELIMITED_TITLE})")
                    .withReference("customid", customId("fencescode"))
                    .build(),
            groupNames = listOf("fenceschar", "fencescodelang", "fencescodecaption", "fencescodecustomid"),
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
                "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\R+|$)",
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
    ): String =
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
                            ).build(),
                    interruption = "(?:\\n+|$)",
                ),
        )
    }

    /**
     * Definition of a footnote, whose text follows the same rules as a [paragraph] and may start on the next line.
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
                    interruption = "(?:${paragraph.regex})?",
                ),
        )
    }

    /**
     * Item of a list, made of its bullet, an optional task marker, and every following line
     * up to the first one that cannot continue it.
     *
     * A blank line, or a line of whitespace, always continues the item if indented content follows it.
     * Otherwise, a line break ends the item, without being part of it, if a bullet or a line of whitespace follows;
     * it is the last character of the item if a blank line follows.
     * @see ListItemToken
     */
    val listItem by lazy {
        TokenRegexPattern(
            name = "ListItem",
            wrap = ::ListItemToken,
            regex =
                RegexBuilder(
                    "^(( {0,3})(?:bullet))([ \\t]\\[[ xX]\\]|(?:))[ \\t]" +
                        "(?:(?:(?!\\n)|(?=resumption))[\\s\\S]*?(?:$END_OF_INPUT|(?=blocked)(?!resumption)|(?!blocked)(?!resumption)\\n(?=\\n))|)",
                ).withReference("resumption", "\\n\\s*^\\3 {2,}")
                    .withReference("blocked", "\\n(?:\\s+\\n| {0,3}(?:bullet))")
                    .withReference("bullet", PatternHelpers.BULLET)
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
                "^(?: *(?:\\n|$))+",
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
     * Plain text content: a non-empty line, continued by every following non-empty line that is not an interruption.
     * @see ParagraphToken
     */
    val paragraph by lazy {
        TokenRegexPattern(
            name = "Paragraph",
            wrap = ::ParagraphToken,
            regex =
                RegexBuilder("([^\\n][\\s\\S]*?(?:$END_OF_INPUT|(?=\\n(?:\\n|$END_OF_INPUT|interruption))))")
                    .withReference("interruption", interruptionRule().pattern)
                    .build(),
        )
    }

    /**
     * Heading underlined by `=` or `-`: one or more non-empty lines that do not start with a bullet,
     * up to the first underline. The heading text spans every line up to the underline (exclusive).
     * @see SetextHeadingToken
     */
    val setextHeading by lazy {
        TokenRegexPattern(
            name = "SetextHeading",
            wrap = ::SetextHeadingToken,
            regex =
                RegexBuilder(
                    "^notbullet" +
                        "((?:(?>[^\\r\\n][\\s\\S]*?(?=\\R(?:\\R|barline|(?: {0,3}(?:bullet))|[^\\n]*\\Rbarline)))\\R)?" +
                        "notbullet(?!barline).+?)customid?\\Rbar *(?:\\R+|$)",
                ).withReference("barline", " {0,3}(?:=+|-+) *(?:\\R|$)")
                    .withReference("bar", " {0,3}(=+|-+)")
                    .withReference("notbullet", "(?! {0,3}(?:bullet))")
                    .withReference("bullet", PatternHelpers.BULLET)
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
                        // Cells: every following line up to a blank line or an interruption
                        "(?:\\n((?:(?! *\\n|interruption)[\\s\\S]*?(?:$END_OF_INPUT|\\n(?=$END_OF_INPUT| *\\n|interruption)))?)\\n*|$)",
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
     * Builds the pattern of a list, which begins with a bullet and spans every following line
     * up to the first one that cannot continue it.
     *
     *  A line break ends the list, without being part of it, if the next line is an interruption
     * (including a bullet of a different kind) that is not resumed by indented content or a bullet of the same kind.
     * A line break is the last character of the list if two blank lines follow,
     * or if a blank line follows without being resumed.
     *
     * @param bulletInitialization pattern of the bullet that starts the list
     * @param bulletContinuation pattern of a bullet that continues the list
     */
    private fun listPattern(
        bulletInitialization: String,
        bulletContinuation: String,
    ): String {
        val resumption = "\\s*^(?: {2,}| {0,3}$bulletContinuation[ \\t])"
        val interrupted = "\\n(?:interruption)"
        val endBeforeInterruption = "(?=$interrupted)(?!\\n$resumption)"
        val endAfterBlank = "(?!$interrupted)\\n(?=(?:\\s*\\n){2}|\\n(?!$resumption))"
        val lines = "[\\s\\S]*?(?:$END_OF_INPUT|$endBeforeInterruption|$endAfterBlank)"
        // The first line's content may be empty only if the list resumes right after it.
        val firstLine = "(?:(?!\\n)|(?=\\n$resumption))"

        return RegexBuilder("^(( {0,3}$bulletInitialization)[ \\t](?:$firstLine$lines|))")
            .withReference("interruption", interruptionRule(includeList = false).pattern)
            .withReference("list", " {0,3}(?:[*+-]|\\d[.)]) ")
            .build()
    }
}
