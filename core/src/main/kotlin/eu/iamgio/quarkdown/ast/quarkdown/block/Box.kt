package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.inline.CodeSpan
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.misc.Color
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

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
         * @param title additional error title
         * @return a custom box containing the error message
         */
        fun error(
            message: String,
            title: String? = null,
        ) = Box(
            title = listOf(Text("Error" + if (title != null) ": $title" else "")),
            padding = null,
            backgroundColor = Color(224, 67, 64),
            foregroundColor = Color(255, 255, 255),
            children = listOf(Paragraph(listOf(CodeSpan(message)))),
        )
    }
}
