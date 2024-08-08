package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

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
    val gap: Size?,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible orientation types of a [Stacked] block.
     */
    enum class Orientation : RenderRepresentable {
        HORIZONTAL,
        VERTICAL,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
    }

    /**
     * Possible alignment types along the main axis of a [Stacked] block.
     */
    enum class MainAxisAlignment : RenderRepresentable {
        START,
        CENTER,
        END,
        SPACE_BETWEEN,
        SPACE_AROUND,
        SPACE_EVENLY,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
    }

    /**
     * Possible alignment types along the cross axis of a [Stacked] block.
     */
    enum class CrossAxisAlignment : RenderRepresentable {
        START,
        CENTER,
        END,
        STRETCH,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
    }
}
