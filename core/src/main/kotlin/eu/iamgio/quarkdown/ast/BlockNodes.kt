package eu.iamgio.quarkdown.ast

/**
 * A new line.
 */
class Newline : Node {
    override fun toString() = "Newline"
}

/**
 * A code block defined via fences.
 * Example:
 * ~~~
 * ```lang
 * Code
 * ```
 * ~~~
 * Alternative:
 * ```
 *     Code
 * ```
 */
data class Code(
    override val text: String,
    val language: String?,
) : TextNode

/**
 * A horizontal line.
 * Example:
 * ```
 * ---
 * ```
 */
class HorizontalRule : Node {
    override fun toString() = "HorizontalRule"
}

/**
 * A heading defined via prefix symbols.
 * Example:
 * ```
 * # Heading
 * ```
 * Alternative:
 * ```
 * Heading
 * ====
 * ```
 */
data class Heading(
    val depth: Int,
    override val text: String,
) : TextNode

/**
 * Creation of a link reference.
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 */
data class LinkDefinition(
    override val text: String,
    val url: String,
    val title: String?,
) : TextNode

/**
 * A list, either ordered or unordered.
 */
interface ListBlock : NestableNode {
    val isTask: Boolean
}

/**
 * An unordered list.
 * Example:
 * ```
 * - A
 * - B
 * ```
 */
data class UnorderedList(
    override val isTask: Boolean,
    override val children: List<Node>,
) : ListBlock

/**
 * An ordered list.
 * Example:
 * ```
 * 1. First
 * 2. Second
 * ```
 */
data class OrderedList(
    override val isTask: Boolean,
    override val children: List<Node>,
) : ListBlock

/**
 * An item of a [ListBlock].
 */
data class ListItem(
    override val children: List<Node>,
) : NestableNode

/**
 * An HTML block.
 * Example:
 * ```
 * <p>
 *     Code
 * </p>
 * ```
 */
data class Html(
    val content: String,
) : Node

/**
 * A text paragraph.
 */
data class Paragraph(
    override val text: String,
) : TextNode

/**
 * A block quote.
 * Example:
 * ```
 * > Quote
 * ```
 */
data class BlockQuote(
    override val children: List<Node>,
) : NestableNode

/**
 * Anything else (should not happen).
 */
class BlockText : Node
