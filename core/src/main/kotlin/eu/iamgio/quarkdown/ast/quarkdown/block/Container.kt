package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes
import eu.iamgio.quarkdown.misc.color.Color
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A general-purpose container that groups content.
 * @param width width of the container
 * @param height height of the container
 * @param fullWidth whether the container should take up the full width of the parent. Overridden by [width]
 * @param foregroundColor text color
 * @param backgroundColor background color
 * @param borderColor border color
 * @param borderWidth border width
 * @param borderStyle border style
 * @param margin whitespace outside the content
 * @param padding whitespace around the content
 * @param cornerRadius border radius of the container
 * @param alignment alignment of the content
 */
class Container(
    val width: Size? = null,
    val height: Size? = null,
    val fullWidth: Boolean = false,
    val foregroundColor: Color? = null,
    val backgroundColor: Color? = null,
    val borderColor: Color? = null,
    val borderWidth: Sizes? = null,
    val borderStyle: BorderStyle? = null,
    val margin: Sizes? = null,
    val padding: Sizes? = null,
    val cornerRadius: Sizes? = null,
    val alignment: Aligned.Alignment? = null,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    /**
     * Style of the border of a [Container].
     */
    enum class BorderStyle : RenderRepresentable {
        /**
         * Solid border.
         */
        NORMAL,

        /**
         * Dashed border.
         */
        DASHED,

        /**
         * Dotted border.
         */
        DOTTED,

        /**
         * Double border.
         */
        DOUBLE,

        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}
