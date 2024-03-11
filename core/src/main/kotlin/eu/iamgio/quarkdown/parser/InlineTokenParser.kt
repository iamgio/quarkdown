package eu.iamgio.quarkdown.parser

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.parser.visitor.InlineTokenVisitor

/**
 * A parser for inline tokens.
 * @param flavor flavor to use in order to analyze and parse sub-tokens
 */
class InlineTokenParser(private val flavor: MarkdownFlavor) : InlineTokenVisitor<Node> {
    override fun visit(token: PlainTextToken): Node {
        TODO("Not yet implemented")
    }
}
