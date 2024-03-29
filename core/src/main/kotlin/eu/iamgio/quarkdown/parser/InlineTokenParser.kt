package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strikethrough
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.ast.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.lexer.CodeSpanToken
import eu.iamgio.quarkdown.lexer.CommentToken
import eu.iamgio.quarkdown.lexer.CriticalContentToken
import eu.iamgio.quarkdown.lexer.DiamondAutolinkToken
import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.EntityToken
import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.ImageToken
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.LinkToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.ReferenceImageToken
import eu.iamgio.quarkdown.lexer.ReferenceLinkToken
import eu.iamgio.quarkdown.lexer.StrikethroughToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.UrlAutolinkToken
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.nextOrNull
import eu.iamgio.quarkdown.util.trimDelimiters
import eu.iamgio.quarkdown.visitor.token.InlineTokenVisitor
import org.apache.commons.text.StringEscapeUtils

/**
 * ASCII of the character that replaces null characters,
 * following CommonMark's security guideline _(2.3 Insecure characters)_.
 */
private const val NULL_CHAR_REPLACEMENT_ASCII = 65533

/**
 * A parser for inline tokens.
 * @param flavor flavor to use in order to analyze and parse sub-tokens
 * @param context additional data to fill during the parsing process
 */
class InlineTokenParser(
    private val flavor: MarkdownFlavor,
    private val context: MutableContext,
) :
    InlineTokenVisitor<Node> {
    /**
     * @return the parsed content of the tokenization from [this] lexer
     */
    private fun Lexer.tokenizeAndParse(): List<Node> =
        this.tokenize()
            .acceptAll(flavor.parserFactory.newParser(context))

    /**
     * Tokenizes and parses sub-nodes.
     * @param source source to tokenize using the default inline lexer from this [flavor]
     * @return parsed nodes
     */
    private fun parseSubContent(source: CharSequence) = flavor.lexerFactory.newInlineLexer(source).tokenizeAndParse()

    /**
     * Tokenizes and parses sub-nodes within a link label.
     * @param source source to tokenize using the link label inline lexer from this [flavor]
     * @return parsed nodes
     */
    private fun parseLinkLabelSubContent(source: CharSequence) = flavor.lexerFactory.newLinkLabelInlineLexer(source).tokenizeAndParse()

    override fun visit(token: EscapeToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Text(text = groups.next())
    }

    override fun visit(token: EntityToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val entity = groups.next().trim().lowercase()

        /**
         * @param radix radix to decode the numeric value for (`radix = 10` for decimal, `radix = 16` for hexadecimal)
         * @return [this] string to its corresponding character in [radix] representation.
         */
        fun String.decodeToContent(radix: Int): String {
            val ascii = toIntOrNull(radix) ?: return ""
            // CommonMark's security guideline (2.3 Insecure characters)
            return if (ascii != 0) {
                ascii.toChar()
            } else {
                NULL_CHAR_REPLACEMENT_ASCII.toChar()
            }.toString()
        }

        // Critical because further checks and mappings may be required during the rendering stage.
        return CriticalContent(
            when {
                entity == "colon" -> ":"
                // Hexadecimal (e.g. &#xD06)
                entity.startsWith("#x") -> groups.next().decodeToContent(radix = 16)
                // Decimal (e.g. &#35)
                entity.startsWith("#") -> groups.next().decodeToContent(radix = 10)
                // HTML entity (e.g. &nbsp;)
                else -> StringEscapeUtils.unescapeHtml4(token.data.text)
            },
        )
    }

    override fun visit(token: CriticalContentToken): Node {
        return CriticalContent(token.data.text)
    }

    override fun visit(token: CommentToken): Node {
        // Content is ignored.
        return Comment()
    }

    override fun visit(token: LineBreakToken): Node {
        return LineBreak()
    }

    override fun visit(token: LinkToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Link(
            label = parseLinkLabelSubContent(groups.next()),
            url = groups.next().trim(),
            // Removes leading and trailing delimiters.
            title = groups.nextOrNull()?.trimDelimiters()?.trim(),
        )
    }

    override fun visit(token: ReferenceLinkToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val label = parseLinkLabelSubContent(groups.next())
        // When the reference is collapsed, the label is the same as the reference label.
        return ReferenceLink(
            label = label,
            reference = groups.nextOrNull()?.let { parseLinkLabelSubContent(it) } ?: label,
            fallback = { Text(token.data.text) },
        )
    }

    override fun visit(token: DiamondAutolinkToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val url = groups.next().trim()
        return visit(UrlAutolinkToken(token.data.copy(text = url)))
    }

    override fun visit(token: UrlAutolinkToken): Node {
        val url = token.data.text.trim()
        return Link(
            label = listOf(Text(url)),
            url = url,
            title = null,
        )
    }

    override fun visit(token: ImageToken): Node {
        val link = visit(LinkToken(token.data)) as Link
        return Image(link)
    }

    override fun visit(token: ReferenceImageToken): Node {
        val link = visit(ReferenceLinkToken(token.data)) as ReferenceLink
        return ReferenceImage(link)
    }

    override fun visit(token: CodeSpanToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 3)
        val text = groups.next().replace("\n", " ")

        // If the text start and ends by a space, and does contain non-space characters,
        // the leading and trailing spaces are trimmed (according to CommonMark).
        val hasNonSpaceChars = text.any { it != ' ' }
        val hasSpaceCharsOnBothEnds = text.firstOrNull() == ' ' && text.lastOrNull() == ' '

        return CodeSpan(
            if (hasNonSpaceChars && hasSpaceCharsOnBothEnds) {
                text.trimDelimiters()
            } else {
                text
            },
        )
    }

    override fun visit(token: PlainTextToken): Node {
        return Text(token.data.text)
    }

    /**
     * @param token emphasis token to parse the content for
     * @return parsed content of an emphasis token
     */
    private fun emphasisContent(token: Token): InlineContent {
        // The raw string content, without the delimiters.
        val text = token.data.groups.iterator(consumeAmount = 3).next()
        return parseSubContent(text)
    }

    override fun visit(token: EmphasisToken): Node {
        return Emphasis(emphasisContent(token))
    }

    override fun visit(token: StrongToken): Node {
        return Strong(emphasisContent(token))
    }

    override fun visit(token: StrongEmphasisToken): Node {
        return StrongEmphasis(emphasisContent(token))
    }

    override fun visit(token: StrikethroughToken): Node {
        return Strikethrough(emphasisContent(token))
    }
}
