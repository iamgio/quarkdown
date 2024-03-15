@file:Suppress("ktlint:standard:no-wildcard-imports")

package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.CriticalContent
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.Image
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.ReferenceImage
import eu.iamgio.quarkdown.ast.ReferenceLink
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.lexer.*
import eu.iamgio.quarkdown.parser.visitor.InlineTokenVisitor
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.nextOrNull
import org.apache.commons.text.StringEscapeUtils

/**
 * ASCII of the character that replaces null characters,
 * following CommonMark's security guideline _(2.3 Insecure characters)_.
 */
private const val NULL_CHAR_REPLACEMENT_ASCII = 65533

/**
 * A parser for inline tokens.
 * @param flavor flavor to use in order to analyze and parse sub-tokens
 */
class InlineTokenParser(private val flavor: MarkdownFlavor) : InlineTokenVisitor<Node> {
    /**
     * Tokenizes and parses sub-nodes.
     * @param lexer lexer to use to tokenize
     * @return parsed nodes
     */
    private fun parseSubContent(lexer: Lexer) = lexer.tokenize().acceptAll(flavor.parserFactory.newParser())

    /**
     * Tokenizes and parses sub-nodes.
     * @param source source to tokenize using the default inline lexer from this [flavor]
     * @return parsed nodes
     */
    private fun parseSubContent(source: CharSequence) = parseSubContent(flavor.lexerFactory.newInlineLexer(source))

    /**
     * Tokenizes and parses sub-nodes within a link label.
     * @param source source to tokenize using the link label inline lexer from this [flavor]
     * @return parsed nodes
     */
    private fun parseLinkLabelSubContent(source: CharSequence) = parseSubContent(flavor.lexerFactory.newLinkLabelInlineLexer(source))

    override fun visit(token: EscapeToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return PlainText(text = groups.next())
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
            title = groups.nextOrNull()?.run { substring(1, length - 1) }?.trim(),
        )
    }

    override fun visit(token: ReferenceLinkToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val label = groups.next()
        // When the reference is collapsed, the label is the same as the reference label.
        return ReferenceLink(
            label = parseLinkLabelSubContent(label),
            reference = groups.nextOrNull() ?: label,
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
            label = listOf(PlainText(url)),
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

    override fun visit(token: PlainTextToken): Node {
        return PlainText(token.data.text)
    }

    override fun visit(token: EmphasisToken): Node {
        val text = token.data.groups.iterator(consumeAmount = 3).next()
        return Emphasis(children = parseSubContent(text))
    }

    override fun visit(token: StrongToken): Node {
        val text = token.data.groups.iterator(consumeAmount = 3).next()
        return Strong(children = parseSubContent(text))
    }

    override fun visit(token: StrongEmphasisToken): Node {
        val text = token.data.groups.iterator(consumeAmount = 3).next()
        return StrongEmphasis(children = parseSubContent(text))
    }
}
