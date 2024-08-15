package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A block that contains nodes in a grid layout.
 * @param columnCount number of columns
 * @param gap space between rows and columns. If `null`, the default value is used
 */
data class Grid(
    val columnCount: Int,
    val gap: Size?,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
