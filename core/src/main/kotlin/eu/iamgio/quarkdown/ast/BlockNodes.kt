package eu.iamgio.quarkdown.ast

/**
 * A blank line.
 */
class Newline : Node {
    override fun toString() = "Newline"
}

/**
 * A code block defined via fences.
 * @param text code content
 * @param language optional syntax language
 */
data class Code(
    override val text: String,
    val language: String?,
) : TextNode

/**
 * A horizontal line (thematic break).
 */
class HorizontalRule : Node {
    override fun toString() = "HorizontalRule"
}

/**
 * A heading defined via prefix symbols.
 * @param depth importance (`depth=1` for H1, `depth=6` for H6)
 */
data class Heading(
    val depth: Int,
    override val text: String,
) : TextNode

/**
 * Creation of a link reference.
 * @param text label to be referenced
 * @param url associated URL
 * @param title optional reference title
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
    /**
     * Whether the list is loose.
     */
    val isLoose: Boolean
}

/**
 * An unordered list.
 * @param isLoose whether the list is loose
 * @param children items
 */
data class UnorderedList(
    override val isLoose: Boolean,
    override val children: List<Node>,
) : ListBlock

/**
 * An ordered list.
 * @param isLoose whether the list is loose
 * @param children items
 * @param startIndex index of the first item
 */
data class OrderedList(
    val startIndex: Int,
    override val isLoose: Boolean,
    override val children: List<Node>,
) : ListBlock

/**
 * An item of a [ListBlock].
 * @param children content
 */
data class ListItem(
    override val children: List<Node>,
) : NestableNode

/**
 * An HTML block.
 * @param content raw HTML content
 */
data class Html(
    val content: String,
) : Node

/**
 * A text paragraph.
 * @param text text content
 */
data class Paragraph(
    override val text: String,
) : TextNode

/**
 * A block quote.
 * @param children content
 */
data class BlockQuote(
    override val children: List<Node>,
) : NestableNode

/**
 * Anything else (should not happen).
 */
class BlockText : Node
