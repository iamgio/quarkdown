package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.misc.Color
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

typealias InlineContent = List<Node>

/**
 * A [Node] that contains plain text.
 * @see eu.iamgio.quarkdown.util.toPlainText
 */
interface PlainTextNode : Node {
    val text: String
}

/**
 * A comment whose content is ignored.
 */
class Comment : Node {
    override fun toString() = "Comment"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A hard line break.
 */
class LineBreak : Node {
    override fun toString() = "LineBreak"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Content (usually a single character) that requires special treatment during the rendering stage.
 * @param text wrapped text
 */
data class CriticalContent(override val text: String) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A link.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
data class Link(
    override val label: InlineContent,
    override val url: String,
    override val title: String?,
) : LinkNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A link that references a [LinkDefinition].
 * @param label inline content of the displayed label
 * @param reference label of the [LinkDefinition] this link points to
 * @param fallback supplier of the node to show instead of [label] in case the reference is invalid
 */
data class ReferenceLink(
    val label: InlineContent,
    val reference: InlineContent,
    val fallback: () -> Node,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An image.
 * @param link the link the image points to
 * @param width optional width constraint
 * @param height optional height constraint
 */
data class Image(
    val link: LinkNode,
    val width: Int?,
    val height: Int?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An images that references a [LinkDefinition].
 * @param link the link the image references
 * @param width optional width constraint
 * @param height optional height constraint
 */
data class ReferenceImage(
    val link: ReferenceLink,
    val width: Int?,
    val height: Int?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An immutable checkbox that is either checked or unchecked.
 * @param isChecked whether the checkbox is checked
 * @see TaskListItem
 */
data class CheckBox(
    val isChecked: Boolean,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

// Emphasis

/**
 * Inline code.
 * @param text text content
 * @param content additional content this code holds, if any
 */
data class CodeSpan(
    override val text: String,
    val content: ContentInfo? = null,
) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Additional content a [CodeSpan] may hold.
     */
    sealed interface ContentInfo

    /**
     * A color linked to a [CodeSpan].
     * For instance, this content may be assigned to a [CodeSpan] if its text holds information about a color's hex.
     * @param color color data
     */
    data class ColorContent(val color: Color) : ContentInfo
}

/**
 * Plain inline text.
 * @param text text content.
 */
data class Text(override val text: String) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Weakly emphasized content.
 * @param children content
 */
data class Emphasis(override val children: InlineContent) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Strongly emphasized content.
 * @param children content
 */
data class Strong(override val children: InlineContent) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Heavily emphasized content.
 * @param children content
 */
data class StrongEmphasis(override val children: InlineContent) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Strikethrough content.
 * @param children content
 */
data class Strikethrough(override val children: InlineContent) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A math (TeX) inline.
 * @param expression expression content
 */
data class MathSpan(
    val expression: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
