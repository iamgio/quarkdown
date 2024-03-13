package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.Comment
import eu.iamgio.quarkdown.ast.Emphasis
import eu.iamgio.quarkdown.ast.LineBreak
import eu.iamgio.quarkdown.ast.Link
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PlainText
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.ast.StrongEmphasis
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.lexer.CommentToken
import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.EscapeToken
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.LineBreakToken
import eu.iamgio.quarkdown.lexer.LinkToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.parser.visitor.InlineTokenVisitor
import eu.iamgio.quarkdown.util.iterator
import eu.iamgio.quarkdown.util.nextOrNull

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

    override fun visit(token: EscapeToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return PlainText(text = groups.next())
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
            label = parseSubContent(flavor.lexerFactory.newLinkLabelInlineLexer(groups.next())),
            url = groups.next(),
            // Removes leading and trailing delimiters.
            title = groups.nextOrNull()?.run { substring(1, length - 1) },
        )
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
