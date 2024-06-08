package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.SizeUnit
import eu.iamgio.quarkdown.misc.Color
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
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param gap space between nodes
 */
data class Stacked(
    val orientation: Orientation,
    val mainAxisAlignment: MainAxisAlignment,
    val crossAxisAlignment: CrossAxisAlignment,
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

    /**
     * Possible alignment types along the main axis of a [Stacked] block.
     */
    enum class MainAxisAlignment {
        START,
        CENTER,
        END,
        SPACE_BETWEEN,
        SPACE_AROUND,
        SPACE_EVENLY,
    }

    /**
     * Possible alignment types along the cross axis of a [Stacked] block.
     */
    enum class CrossAxisAlignment {
        START,
        CENTER,
        END,
        STRETCH,
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
 * @param padding padding of the box. If `null`, the box uses the default value.
 * @param backgroundColor background color of the box. If `null`, the box uses the default value.
 * @param foregroundColor foreground color of the box. If `null`, the box uses the default value.
 * @param children content of the box
 */
data class Box(
    val title: InlineContent?,
    val padding: Size?,
    val backgroundColor: Color?,
    val foregroundColor: Color?,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    companion object {
        /**
         * A custom box that shows an error message.
         * @param message error message to display
         * @return a custom box containing the error message
         */
        fun error(message: String) =
            Box(
                title = listOf(Text("Error")),
                padding = Size(8.0, SizeUnit.PX),
                backgroundColor = Color(224, 67, 64),
                foregroundColor = Color(255, 255, 255),
                children = listOf(Text(message)),
            )
    }
}
