@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.BlockTextToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.OrderedListToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.UnorderedListToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 * Regex patterns for [BaseMarkdownFlavor].
 */
open class BaseMarkdownBlockTokenRegexPatterns {
    /**
     * The rules that defines when a text node must interrupt.
     * This might be overridden by subclasses to add new interruptions.
     *
     * Note: remember to add the `list` reference when using this pattern.
     */
    open val interruptionRule =
        RegexBuilder("hr|heading|blockquote|fences|list|html|table| +\\n")
            .withReference("hr", horizontalRule.regex.pattern) // Interrupts on horizontal rule
            .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
            .withReference("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n")
            .withReference("html", "<\\/?(?:tag)(?: +|\\n|\\/?>)|<(?:script|pre|style|textarea|!--)")
            .withReference("blockquote", " {0,3}>")
            .withReference("tag", TAG_HELPER)
            .build()

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
                    "^ {0,3}(#{1,6})(?=\\s|$)(.*)(?:\\n+|$)"
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
                    "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\n+|$)"
                        .toRegex(),
            )

    /**
     * HTML content.
     * @see HtmlToken
     */
    val html
        get() =
            TokenRegexPattern(
                name = "HTML",
                wrap = ::HtmlToken,
                regex =
                    RegexBuilder(HTML_HELPER)
                        .withReference("comment", COMMENT_HELPER)
                        .withReference("tag", TAG_HELPER)
                        .withReference(
                            "attribute",
                            " +[a-zA-Z:_][\\w.:-]*(?: *= *\"[^\"\\n]*\"| *= *'[^'\\n]*'| *= *[^\\s\"'=<>`]+)?",
                        )
                        .build(),
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
                        )
                        .build(),
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
                    )
                        .withReference("bullet", BULLET_HELPER)
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
                        .withReference("interruption", interruptionRule.pattern)
                        .withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
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
                    RegexBuilder("^(?!bullet )((?:.|\\R(?!\\s*?\\n|bullet ))+?)\\R {0,3}(=+|-+) *(?:\\R+|$)")
                        .withReference("bullet", BULLET_HELPER)
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
            .withReference("interruption", interruptionRule.pattern)
            .withReference("list", " {0,3}(?:[*+-]|\\d[.)]) ")
            .build()
    }
}

// Helper expressions

private const val BULLET_HELPER = "[*+-]|\\d{1,9}[\\.)]"

internal const val TAG_HELPER =
    "address|article|aside|base|basefont|blockquote|body|caption" +
        "|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption" +
        "|figure|footer|form|frame|frameset|h[1-6]|head|header|hr|html|iframe" +
        "|legend|li|link|main|menu|menuitem|meta|nav|noframes|ol|optgroup|option" +
        "|p|param|search|section|summary|table|tbody|td|tfoot|th|thead|title" +
        "|tr|track|ul"

private const val HTML_HELPER =
    "^ {0,3}(?:" +
        "<(script|pre|style|textarea)[\\s>][\\s\\S]*?(?:<\\/\\1>[^\\n]*\\n+)" +
        "|comment[^\\n]*(\\n+)" +
        "|<\\?[\\s\\S]*?(?:\\?>\\n*)" +
        "|<![A-Z][\\s\\S]*?(?:>\\n*)" +
        "|<!\\[CDATA\\[[\\s\\S]*?(?:\\]\\]>\\n*)" +
        "|<\\/?(tag)(?: +|\\n|\\/?>)[\\s\\S]*?(?:(?:\\n *)+\\n)" +
        "|<(?!script|pre|style|textarea)([a-z][\\w-]*)(?:attribute)*? *\\/?>(?=[ \\t]*(?:\\n))[\\s\\S]*?(?:(?:\\n *)+\\n)" +
        "|<\\/(?!script|pre|style|textarea)[a-z][\\w-]*\\s*>(?=[ \\t]*(?:\\n))[\\s\\S]*?(?:(?:\\n *)+\\n)" +
        ")"

internal const val COMMENT_HELPER = "<!--(?:-?>|[\\s\\S]*?(?:-->))"
