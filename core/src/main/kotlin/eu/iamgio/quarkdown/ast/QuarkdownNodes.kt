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

/**
 * A block whose content is clipped in a path.
 * @param clip type of the clip path
 */
data class Clipped(
    val clip: Clip,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible clip types of a [Clipped] block.
     */
    enum class Clip {
        CIRCLE,
    }
}
