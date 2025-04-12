package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A collapsible block, whose content can be hidden or shown by interacting with it.
 * @param title title of the block
 * @param isOpen whether the block is open at the beginning
 */
class Collapse(
    val title: InlineContent,
    val isOpen: Boolean,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
