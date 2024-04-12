package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

// Nodes that aren't parsed from the source Markdown input,
// but rather returned from Quarkdown functions.

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
