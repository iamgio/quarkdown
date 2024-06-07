package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

// Nodes that aren't parsed from the source Markdown input,
// but rather returned from and/or handled by Quarkdown functions.

/**
 * A generic group of block nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.ValueFactory.blockMarkdown
 */
data class MarkdownContent(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}

/**
 * A generic group of inline nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.ValueFactory.inlineMarkdown
 */
data class InlineMarkdownContent(
    override val children: InlineContent,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}

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
 * A block that contains nodes stacked along a certain [orientation].
 * @param orientation orientation of the stack
 * @param gap space between nodes
 */
data class Stacked(
    val orientation: Orientation,
    val gap: Size,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible orientation types of a [Stacked] block.
     */
    enum class Orientation {
        HORIZONTAL,
        VERTICAL,
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

/**
 * A generic box that contains content.
 * @param title box title. If `null`, the box is untitled.
 * @param children content of the box
 */
data class Box(
    val title: InlineContent?,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
