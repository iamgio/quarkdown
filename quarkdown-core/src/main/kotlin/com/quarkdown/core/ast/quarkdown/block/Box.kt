package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.dsl.buildBlocks
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A generic box that contains content.
 * @param title box title. If `null`, the box is untitled
 * @param type type of the box
 * @param padding padding of the box. If `null`, the box uses the default value
 * @param backgroundColor background color of the box. If `null`, the box uses the default value
 * @param foregroundColor foreground color of the box. If `null`, the box uses the default value
 * @param children content of the box
 */
class Box(
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
         * A tip.
         */
        TIP,

        /**
         * A note.
         */
        NOTE,

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
         * A box that shows an error content with a monospaced text content.
         * @param content error message to display
         * @param title additional error title
         * @return a box containing the error message
         */
        fun error(
            content: List<Node>,
            title: String? = null,
        ) = Box(
            title =
                buildInline {
                    text("Error" + if (title != null) ": $title" else "")
                },
            type = Type.ERROR,
            children = content,
        )

        /**
         * A box that shows an error content with an optional source code snippet.
         * @param message error message to display
         * @param title additional error title
         * @param sourceText optional source code snippet to display
         * @return a box containing the error message
         */
        fun error(
            message: InlineContent,
            title: String? = null,
            sourceText: CharSequence?,
        ): Box {
            val content =
                buildBlocks {
                    +Paragraph(message)
                    sourceText?.let {
                        +Code(
                            it.toString(),
                            language = null,
                            highlight = false,
                            showLineNumbers = false,
                        )
                    }
                }
            return error(content, title)
        }
    }
}
