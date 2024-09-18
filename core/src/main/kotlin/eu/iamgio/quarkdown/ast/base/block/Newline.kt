package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A blank line.
 */
data object Newline : TextNode {
    override val text: List<Node> = emptyList()

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
