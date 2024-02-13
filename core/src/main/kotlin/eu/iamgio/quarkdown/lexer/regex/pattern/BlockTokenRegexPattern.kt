package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.lexer.regex.RegexBuilder
import eu.iamgio.quarkdown.lexer.type.BlockTokenType
import eu.iamgio.quarkdown.lexer.type.TokenType

// Some of the following patterns were taken or inspired by https://github.com/markedjs/marked/blob/master/src/rules.ts

/**
 * Collection of [TokenRegexPattern]s that match macro-blocks.
 */
// TODO add test
enum class BlockTokenRegexPattern(override val tokenType: TokenType, override val regex: Regex) : TokenRegexPattern {
    BLOCKQUOTE(
        BlockTokenType.BLOCKQUOTE,
        RegexBuilder("^( {0,3}> ?(paragraph|[^\\n]*)(?:\\n|$))+")
            .withReference("paragraph", PARAGRAPH_PATTERN.pattern)
            .build(RegexOption.MULTILINE),
    ),
    BLOCKCODE(
        BlockTokenType.BLOCK_CODE,
        "^( {4}[^\\n]+(?:\\n(?: *(?:\\n|\$))*)?)+"
            .toRegex(RegexOption.MULTILINE),
    ), // TODO check wrong detection instead of list paragraph and HTML content
    LINKDEFINITION(
        BlockTokenType.LINK_DEFINITION,
        RegexBuilder("^ {0,3}\\[(label)\\]: *(?:\\n *)?([^<\\s][^\\s]*|<.*?>)(?:(?: +(?:\\n *)?| *\\n *)(title))? *(?:\\n+|$)")
            .withReference("label", BLOCK_LABEL_HELPER)
            .withReference("title", "(?:\"(?:\\\\\"?|[^\"\\\\])*\"|'[^'\\n]*(?:\\n[^'\\n]+)*\\n?'|\\([^()]*\\))")
            .build(),
    ),
    FENCESCODE(
        BlockTokenType.FENCES_CODE,
        "^ {0,3}((`{3,})(\\s*.+\\R)?((.|\\s)+?)(`{3,}))|((~{3,})(\\s*.+\\R)?((.|\\s)+?)(~{3,}))"
            .toRegex(),
    ),
    HEADING(
        BlockTokenType.HEADING,
        "^ {0,3}(#{1,6})(?=\\s|$)(.*)(?:\\n+|$)"
            .toRegex(),
    ),
    HORIZONTALRULE(
        BlockTokenType.HORIZONTAL_RULE,
        HORIZONTAL_RULE_HELPER
            .toRegex(),
    ),
    SETEXTHEADING(
        BlockTokenType.SETEXT_HEADING,
        RegexBuilder("^(?!bullet )((?:.|\\R(?!\\s*?\\n|bullet ))+?)\\R {0,3}(=+|-+) *(?:\\R+|$)")
            .withReference("bullet", BULLET_HELPER)
            .withReference("bullet", BULLET_HELPER)
            .build(),
    ), // TODO match might fail
    HTML(
        BlockTokenType.HTML,
        RegexBuilder(HTML_HELPER)
            .withReference("comment", COMMENT_HELPER)
            .withReference("tag", TAG_HELPER)
            .withReference("attribute", " +[a-zA-Z:_][\\w.:-]*(?: *= *\"[^\"\\n]*\"| *= *'[^'\\n]*'| *= *[^\\s\"'=<>`]+)?")
            .build(),
    ),
    LIST(
        BlockTokenType.LIST,
        RegexBuilder("^( {0,3}bullet)([ \\t][^\\n]+?)?(?:\\n|\$)")
            .withReference("bullet", BULLET_HELPER)
            .build(),
    ),
    NEWLINE(
        BlockTokenType.NEWLINE,
        "^(?: *(?:\\n|$))+"
            .toRegex(),
    ),
    PARAGRAPH(
        BlockTokenType.PARAGRAPH,
        PARAGRAPH_PATTERN,
    ),
    BLOCKTEXT(
        BlockTokenType.BLOCK_TEXT,
        "^[^\\n]+"
            .toRegex(),
    ),
}

// Helper expressions

private const val BULLET_HELPER = "[*+-]|\\d{1,9}[\\.)]"

private const val HORIZONTAL_RULE_HELPER = "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\n+|$)"

private const val PARAGRAPH_HELPER = "^([^\\n]+(?:\\n(?!hr|heading|lheading|blockquote|fences|list|html|table| +\\n)[^\\n]+)*)"

private const val BLOCK_LABEL_HELPER = "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+"

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

private val PARAGRAPH_PATTERN =
    RegexBuilder(PARAGRAPH_HELPER)
        .withReference("hr", HORIZONTAL_RULE_HELPER)
        .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
        .withReference("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n")
        .withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
        .withReference("html", "<\\/?(?:tag)(?: +|\\n|\\/?>)|<(?:script|pre|style|textarea|!--)")
        .withReference("blockquote", " {0,3}>")
        .withReference("tag", TAG_HELPER)
        .build(RegexOption.MULTILINE)