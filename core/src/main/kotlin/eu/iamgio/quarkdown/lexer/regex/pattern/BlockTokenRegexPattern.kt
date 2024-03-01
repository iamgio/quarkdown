package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.BlockTextToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.MultilineMathToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.OnelineMathToken
import eu.iamgio.quarkdown.lexer.OrderedListToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.UnorderedListToken
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder

// Some of the following patterns were taken or inspired by https://github.com/markedjs/marked/blob/master/src/rules.ts

/**
 * Collection of [TokenRegexPattern]s that match macro-blocks.
 */
enum class BlockTokenRegexPattern(
    override val wrap: (TokenData) -> Token,
    override val regex: Regex,
) : TokenRegexPattern {
    BLOCKQUOTE(
        ::BlockQuoteToken,
        RegexBuilder("^( {0,3}> ?(paragraph|[^\\n]*)(?:\\n|$))+")
            .withReference("paragraph", PARAGRAPH_PATTERN.pattern)
            .build(),
    ),
    BLOCKCODE(
        ::BlockCodeToken,
        "^( {4}[^\\n]+(?:\\n(?: *(?:\\n|\$))*)?)+"
            .toRegex(),
    ),
    LINKDEFINITION(
        ::LinkDefinitionToken,
        RegexBuilder("^ {0,3}\\[(label)\\]: *(?:\\n *)?([^<\\s][^\\s]*|<.*?>)(?:(?: +(?:\\n *)?| *\\n *)(title))? *(?:\\n+|$)")
            .withReference("label", BLOCK_LABEL_HELPER)
            .withReference("title", "(?:\"(?:\\\\\"?|[^\"\\\\])*\"|'[^'\\n]*(?:\\n[^'\\n]+)*\\n?'|\\([^()]*\\))")
            .build(),
    ),
    FENCESCODE(
        ::FencesCodeToken,
        "^ {0,3}((?<fenceschar>[`~]){3,})($|\\s*.+$)((.|\\s)+?)(\\k<fenceschar>{3,})"
            .toRegex(),
    ),
    MULTILINEMATH(
        ::MultilineMathToken,
        "^ {0,3}(\\\${3,})((.|\\s)+?)(\\\${3,})"
            .toRegex(),
    ),
    ONELINEMATH(
        ::OnelineMathToken,
        ONELINE_MATH_HELPER.toRegex(),
    ),
    HEADING(
        ::HeadingToken,
        HEADING_PATTERN,
    ),
    HORIZONTALRULE(
        ::HorizontalRuleToken,
        HORIZONTAL_RULE_HELPER
            .toRegex(),
    ),
    SETEXTHEADING(
        ::SetextHeadingToken,
        RegexBuilder("^(?!bullet )((?:.|\\R(?!\\s*?\\n|bullet ))+?)\\R {0,3}(=+|-+) *(?:\\R+|$)")
            .withReference("bullet", BULLET_HELPER)
            .withReference("bullet", BULLET_HELPER)
            .build(),
    ),
    HTML(
        ::HtmlToken,
        RegexBuilder(HTML_HELPER)
            .withReference("comment", COMMENT_HELPER)
            .withReference("tag", TAG_HELPER)
            .withReference(
                "attribute",
                " +[a-zA-Z:_][\\w.:-]*(?: *= *\"[^\"\\n]*\"| *= *'[^'\\n]*'| *= *[^\\s\"'=<>`]+)?",
            )
            .build(),
    ),
    UNORDEREDLIST(
        ::UnorderedListToken,
        listPattern(bulletInitialization = "(?<unorderedbull>[*+-])", bulletContinuation = "\\k<unorderedbull>"),
    ),
    ORDEREDLIST(
        ::OrderedListToken,
        listPattern(
            bulletInitialization = "\\d{1,9}(?<orderedbull>[\\.)])",
            bulletContinuation = "\\d{1,9}\\k<orderedbull>",
        ),
    ),
    NEWLINE(
        ::NewlineToken,
        "^(?: *(?:\\n|$))+"
            .toRegex(),
    ),
    PARAGRAPH(
        ::ParagraphToken,
        PARAGRAPH_PATTERN,
    ),
    BLOCKTEXT(
        ::BlockTextToken,
        "^[^\\n]+"
            .toRegex(),
    ),
    LISTITEM(
        ::ListItemToken,
        RegexBuilder("^(( {0,3})(?:bullet))([ \\t]\\[[ xX]\\]|(?:))[ \\t](((.+(\\n(?!(\\s+\\n| {0,3}(bullet))))?)*(\\s*^\\3 {2,})*)*)")
            .withReference("bullet", BULLET_HELPER)
            .withReference("bullet", BULLET_HELPER)
            .build(),
    ),
}

// Helper expressions

private const val BULLET_HELPER = "[*+-]|\\d{1,9}[\\.)]"

private const val ONELINE_MATH_HELPER = " {0,3}\\\$[ \\t](.+?)[ \\t]\\\$\\s*\$"

private const val HORIZONTAL_RULE_HELPER = "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\n+|$)"

private const val BLOCK_LABEL_HELPER = "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+"

private const val TAG_HELPER =
    "address|article|aside|base|basefont|blockquote|body|caption" +
        "|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption" +
        "|figure|footer|form|frame|frameset|h[1-6]|head|header|hr|html|iframe" +
        "|legend|li|link|main|menu|menuitem|meta|nav|noframes|ol|optgroup|option" +
        "|p|param|search|section|summary|table|tbody|td|tfoot|th|thead|title" +
        "|tr|track|ul"

// The rules that defines when a paragraph must interrupt.
// Remember to add the "list" reference when using this pattern.
private val PARAGRAPH_INTERRUPTION_HELPER =
    RegexBuilder("hr|heading|blockquote|fences|mmath|omath|list|html|table| +\\n")
        .withReference("hr", HORIZONTAL_RULE_HELPER) // Interrupts on horizontal rule
        .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
        .withReference("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n")
        .withReference("mmath", " {0,3}(?:\\\${3,})[^\\n]*\\n")
        .withReference("omath", ONELINE_MATH_HELPER)
        .withReference("html", "<\\/?(?:tag)(?: +|\\n|\\/?>)|<(?:script|pre|style|textarea|!--)")
        .withReference("blockquote", " {0,3}>")
        .withReference("tag", TAG_HELPER)
        .build()

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

private val PARAGRAPH_PATTERN =
    RegexBuilder("([^\\n]+(?:\\n(?!interruption)[^\\n]+)*)")
        .withReference("interruption", PARAGRAPH_INTERRUPTION_HELPER.pattern)
        .withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
        .build()

private val HEADING_PATTERN =
    "^ {0,3}(#{1,6})(?=\\s|$)(.*)(?:\\n+|$)"
        .toRegex()

/**
 * Generates a regex pattern that matches a whole list block.
 * @param bulletInitialization bullet pattern that begins the block
 * @param bulletContinuation bullet pattern that continues the block (all the items should ideally share the same bullet type)
 */
private fun listPattern(
    bulletInitialization: String,
    bulletContinuation: String,
): Regex {
    val initialization = "^(( {0,3}$bulletInitialization)[ \\t]((?!^(\\s*\\n){2})"
    val continuation = "(.+(\\n|\$)|\\n\\s*^( {2,}| {0,3}$bulletContinuation[ \\t]))"

    return RegexBuilder("$initialization$continuation(?!^(interruption)))*)+")
        .withReference("interruption", PARAGRAPH_INTERRUPTION_HELPER.pattern)
        .withReference("list", " {0,3}(?:[*+-]|\\d[.)]) ")
        .build()
}
