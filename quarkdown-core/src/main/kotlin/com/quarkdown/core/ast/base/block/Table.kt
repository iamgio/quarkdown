package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A table, consisting of columns, each of which has a header and multiple cells.
 * A table is location-trackable since, if requested by the user, it may show a caption displaying its location-based label.
 * @param columns columns of the table. Each column has a header and multiple cells
 * @param caption optional caption of the table (Quarkdown extension)
 */
class Table(
    val columns: List<Column>,
    override val caption: String? = null,
) : NestableNode,
    LocationTrackableNode,
    CaptionableNode {
    // Exposing all the cell contents as this table's direct children
    // allows visiting them during a tree traversal.
    // If they were isolated, they would be unreachable.
    override val children: List<Node>
        get() =
            columns
                .asSequence()
                .flatMap { it.cells + it.header }
                .flatMap { it.text }
                .toList()

    /**
     * A column of a table.
     * @param alignment text alignment
     * @param header header cell
     * @param cells other cells
     */
    data class Column(
        val alignment: Alignment,
        val header: Cell,
        val cells: List<Cell>,
    )

    /**
     * A single cell of a table.
     * @param text content
     */
    data class Cell(
        val text: InlineContent,
    )

    /**
     * Text alignment of a [Column].
     */
    enum class Alignment : RenderRepresentable {
        LEFT,
        CENTER,
        RIGHT,
        NONE,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
