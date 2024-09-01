package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A block quote.
 * @param attribution additional author or source of the quote
 * @param children content
 */
data class BlockQuote(
    val attribution: InlineContent? = null,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
