@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.lexer.*
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern

/**
 *
 */
open class BaseBlockTokenRegexPatterns {
    val horizontalRule =
        TokenRegexPattern(
            name = "HorizontalRule",
            wrap = ::HorizontalRuleToken,
            regex =
                "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\n+|$)"
                    .toRegex(),
        )

    // The rules that defines when a text node must interrupt.
    // Remember to add the "list" reference when using this TokenRegexPattern.
    open val interruptionRule =
        RegexBuilder("hr|heading|blockquote|fences|mmath|omath|list|html|table| +\\n")
            .withReference("hr", horizontalRule.regex.pattern) // Interrupts on horizontal rule
            .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
            .withReference("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n")
            // .withReference("mmath", " {0,3}(?:\\\${3,})[^\\n]*\\n")
            // .withReference("omath", ONELINE_MATH_HELPER)
            .withReference("html", "<\\/?(?:tag)(?: +|\\n|\\/?>)|<(?:script|pre|style|textarea|!--)")
            .withReference("blockquote", " {0,3}>")
            .withReference("tag", TAG_HELPER)
            .build()

    val paragraph by lazy {
        TokenRegexPattern(
            name = "Paragraph",
            wrap = ::ParagraphToken,
            regex =
                RegexBuilder("([^\\n]+(?:\\n(?!interruption)[^\\n]+)*)")
                    .withReference("interruption", interruptionRule.pattern)
                    .withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
                    .build(),
        )
    }

    val blockCode =
        TokenRegexPattern(
            name = "BlockCode",
            wrap = ::BlockCodeToken,
            regex =
                "^( {4}[^\\n]+(?:\\n(?: *(?:\\n|\$))*)?)+"
                    .toRegex(),
        )

    val fencesCode =
        TokenRegexPattern(
            name = "FencesCode",
            wrap = ::FencesCodeToken,
            regex =
                "^ {0,3}((?<fenceschar>[`~]){3,})($|\\s*.+$)((.|\\s)+?)(\\k<fenceschar>{3,})"
                    .toRegex(),
        )

    val blockQuote =
        TokenRegexPattern(
            name = "BlockQuote",
            wrap = ::BlockQuoteToken,
            regex =
                RegexBuilder("^( {0,3}> ?(paragraph|[^\\n]*)(?:\\n|$))+")
                    .withReference("paragraph", paragraph.regex.pattern)
                    .build(),
        )

    val heading =
        TokenRegexPattern(
            name = "Heading",
            wrap = ::HeadingToken,
            regex =
                "^ {0,3}(#{1,6})(?=\\s|$)(.*)(?:\\n+|$)"
                    .toRegex(),
        )

    val setextHeading =
        TokenRegexPattern(
            name = "SetextHeading",
            wrap = ::SetextHeadingToken,
            regex =
                RegexBuilder("^(?!bullet )((?:.|\\R(?!\\s*?\\n|bullet ))+?)\\R {0,3}(=+|-+) *(?:\\R+|$)")
                    .withReference("bullet", BULLET_HELPER)
                    .withReference("bullet", BULLET_HELPER)
                    .build(),
        )

    val linkDefinition =
        TokenRegexPattern(
            name = "LinkDefinition",
            wrap = ::LinkDefinitionToken,
            regex =
                RegexBuilder("^ {0,3}\\[(label)\\]: *(?:\\n *)?([^<\\s][^\\s]*|<.*?>)(?:(?: +(?:\\n *)?| *\\n *)(title))? *(?:\\n+|$)")
                    .withReference("label", "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+")
                    .withReference("title", "(?:\"(?:\\\\\"?|[^\"\\\\])*\"|'[^'\\n]*(?:\\n[^'\\n]+)*\\n?'|\\([^()]*\\))")
                    .build(),
        )

    val html =
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

    val unorderedList =
        TokenRegexPattern(
            name = "UnorderedList",
            wrap = ::UnorderedListToken,
            regex =
                listPattern(
                    bulletInitialization = "(?<unorderedbull>[*+-])",
                    bulletContinuation = "\\k<unorderedbull>",
                ),
        )

    val orderedList =
        TokenRegexPattern(
            name = "OrderedList",
            wrap = ::OrderedListToken,
            regex =
                listPattern(
                    bulletInitialization = "\\d{1,9}(?<orderedbull>[\\.)])",
                    bulletContinuation = "\\d{1,9}\\k<orderedbull>",
                ),
        )

    val listItem =
        TokenRegexPattern(
            name = "ListItem",
            wrap = ::ListItemToken,
            regex =
                RegexBuilder(
                    "^(( {0,3})(?:bullet))([ \\t]\\[[ xX]\\]|(?:))[ \\t](((.+(\\n(?!(\\s+\\n| {0,3}(bullet))))?)*(\\s*^\\3 {2,})*)*)",
                )
                    .withReference("bullet", BULLET_HELPER)
                    .withReference("bullet", BULLET_HELPER)
                    .build(),
        )

    val newline =
        TokenRegexPattern(
            name = "Newline",
            wrap = ::NewlineToken,
            regex =
                "^(?: *(?:\\n|$))+"
                    .toRegex(),
        )

    val blockText =
        TokenRegexPattern(
            name = "BlockText",
            wrap = ::BlockTextToken,
            regex =
                "^[^\\n]+"
                    .toRegex(),
        )
}

// Helper expressions

private const val BULLET_HELPER = "[*+-]|\\d{1,9}[\\.)]"

private const val TAG_HELPER =
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

private const val COMMENT_HELPER = "<!--(?:-?>|[\\s\\S]*?(?:-->))"
