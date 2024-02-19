package eu.iamgio.quarkdown.ast

/**
 * A new line.
 */
class Newline : Node

/**
 * A code block defined via indentation.
 * Example:
 * ```
 *     Code
 * ```
 */
data class BlockCode(
    val code: String,
) : Node

/**
 * A code block defined via fences.
 * Example:
 * ~~~
 * ```lang
 * Code
 * ```
 * ~~~
 */
data class FencesCode(
    val code: String,
    val lang: String?,
) : Node

/**
 * A horizontal line.
 * Example:
 * ```
 * ---
 * ```
 */
class HorizontalRule : Node

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
    val text: String,
) : Node

/**
 * Creation of a link reference.
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 */
data class LinkDefinition(
    val label: String,
    val url: String,
    val title: String?,
)

/**
 * A list, either ordered or unordered.
 * Examples:
 * ```
 * - A
 * - B
 *
 * 1. First
 * 2. Second
 * ```
 */
data class ListItem(
    val text: String,
    val ordered: Boolean,
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
    val text: String,
    override val children: List<Node>,
) : NestableNode

/**
 * A block quote.
 * Example:
 * ```
 * > Quote
 * ```
 */
data class BlockQuote(
    val text: String,
    override val children: List<Node>,
) : NestableNode

/**
 * Anything else (should not happen).
 */
class BlockText : Node
