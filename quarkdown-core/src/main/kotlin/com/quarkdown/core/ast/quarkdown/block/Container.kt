package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.attributes.style.StylableNode
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A general-purpose container that groups content.
 * @param width width of the container
 * @param height height of the container
 * @param fullWidth whether the container should take up the full width of the parent. Overridden by [width]
 * @param float floating position of the container within the subsequent content
 * @param fullColumnSpan whether the container should span across all columns in a multi-column layout
 * @param className custom class name for the container, useful for applying custom styles, if supported by the renderer
 */
class Container(
    val width: Size? = null,
    val height: Size? = null,
    val fullWidth: Boolean = false,
    val float: FloatAlignment? = null,
    val fullColumnSpan: Boolean = false,
    val className: String? = null,
    override val style: NodeStyle = NodeStyle.DEFAULT,
    @Diverge override val children: List<Node>,
) : NestableNode,
    StylableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    /**
     * Floating position of a [Container].
     */
    enum class FloatAlignment : RenderRepresentable {
        START,
        END,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}
