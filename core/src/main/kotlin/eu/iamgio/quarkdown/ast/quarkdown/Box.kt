package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.CodeSpan
import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.document.page.Size
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
         * @return a custom box containing the error message
         */
        fun error(message: String) =
            Box(
                title = listOf(Text("Error")),
                padding = null,
                backgroundColor = Color(224, 67, 64),
                foregroundColor = Color(255, 255, 255),
                children = listOf(CodeSpan(message)),
            )
    }
}