package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * An aligned block.
 * @param alignment alignment the content should have
 */
data class Aligned(
    val alignment: Alignment,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible alignment types of an [Aligned] block.
     */
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT,
    }
}
