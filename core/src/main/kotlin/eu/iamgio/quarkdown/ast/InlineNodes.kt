package eu.iamgio.quarkdown.ast

/**
 * A comment whose content is ignored.
 */
class Comment : Node {
    override fun toString() = "Comment"
}

/**
 * A hard line break.
 */
class LineBreak : Node {
    override fun toString() = "LineBreak"
}

/**
 * A link.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
data class Link(
    val label: List<Node>,
    val url: String,
    val title: String?,
) : Node

// Emphasis

/**
 * Plain inline text.
 * @param text text content.
 */
data class PlainText(val text: String) : Node

/**
 * Weakly emphasized content.
 * @param children content
 */
data class Emphasis(override val children: List<Node>) : NestableNode

/**
 * Strongly emphasized content.
 * @param children content
 */
data class Strong(override val children: List<Node>) : NestableNode

/**
 * Heavily emphasized content.
 * @param children content
 */
data class StrongEmphasis(override val children: List<Node>) : NestableNode
