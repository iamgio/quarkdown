package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A block that contains nodes grouped together according to the given [layout].
 * @param layout the way nodes are placed together
 * @param mainAxisAlignment content alignment along the main axis
 * @param crossAxisAlignment content alignment along the cross axis
 * @param rowGap vertical space between rows of nodes
 * @param columnGap horizontal space between columns of nodes
 */
class Stacked(
    val layout: Layout,
    val mainAxisAlignment: MainAxisAlignment,
    val crossAxisAlignment: CrossAxisAlignment,
    val rowGap: Size?,
    val columnGap: Size?,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Defines a way nodes of a [Stacked] block are placed together.
     * @see Column
     * @see Row
     * @see Grid
     */
    sealed interface Layout : RenderRepresentable

    /**
     * A layout that stacks nodes vertically.
     */
    data object Column : Layout {
        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    /**
     * A layout that stacks nodes horizontally.
     */
    data object Row : Layout {
        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    /**
     * A layout that stacks nodes in a grid.
     * @param columnCount number of columns
     */
    data class Grid(
        val columnCount: Int,
    ) : Layout {
        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
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
