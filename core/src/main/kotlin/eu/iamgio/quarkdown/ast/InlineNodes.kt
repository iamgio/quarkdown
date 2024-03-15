package eu.iamgio.quarkdown.ast

typealias InlineContent = List<Node>

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
 * Content (usually a single character) that requires special treatment during the rendering stage.
 */
data class CriticalContent(val content: String) : Node

/**
 * A link.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
data class Link(
    val label: InlineContent,
    val url: String,
    val title: String?,
) : Node

/**
 * A link that references a [LinkDefinition].
 * @param label inline content of the displayed label
 * @param reference label of the [LinkDefinition] this link points to
 * @param fallback supplier of the node to show instead of [label] in case the reference is invalid
 */
data class ReferenceLink(
    val label: InlineContent,
    val reference: String,
    val fallback: () -> Node,
) : Node

/**
 * An image.
 * @param link the link the image points to
 */
data class Image(
    val link: Link,
) : Node

/**
 * An images that references a [LinkDefinition].
 * @param link the link the image references
 */
data class ReferenceImage(
    val link: ReferenceLink,
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
data class Emphasis(override val children: InlineContent) : NestableNode

/**
 * Strongly emphasized content.
 * @param children content
 */
data class Strong(override val children: InlineContent) : NestableNode

/**
 * Heavily emphasized content.
 * @param children content
 */
data class StrongEmphasis(override val children: InlineContent) : NestableNode
