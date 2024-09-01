package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.inline.CodeSpan
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.misc.Color
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A generic box that contains content.
 * @param title box title. If `null`, the box is untitled
 * @param type type of the box
 * @param padding padding of the box. If `null`, the box uses the default value
 * @param backgroundColor background color of the box. If `null`, the box uses the default value
 * @param foregroundColor foreground color of the box. If `null`, the box uses the default value
 * @param children content of the box
 */
data class Box(
    val title: InlineContent?,
    val type: Type,
    val padding: Size? = null,
    val backgroundColor: Color? = null,
    val foregroundColor: Color? = null,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible type of [Box], which determines its style.
     */
    enum class Type : RenderRepresentable {
        /**
         * Content with higher importance.
         */
        CALLOUT,

        /**
         * A warning.
         */
        WARNING,

        /**
         * An error.
         */
        ERROR,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }

    companion object {
        /**
         * A box that shows an error message with a monospaced text content.
         * @param message error message to display
         * @param title additional error title
         * @return a box containing the error message
         */
        fun error(
            message: String,
            title: String? = null,
        ) = Box(
            title = listOf(Text("Error" + if (title != null) ": $title" else "")),
            type = Type.ERROR,
            children = listOf(Paragraph(listOf(CodeSpan(message)))),
        )
    }
}
