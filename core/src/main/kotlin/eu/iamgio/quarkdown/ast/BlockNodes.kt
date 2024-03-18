package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.rendering.NodeVisitor

/**
 * A blank line.
 */
class Newline : Node {
    override fun toString() = "Newline"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A code block.
 * @param content code content
 * @param language optional syntax language
 */
data class Code(
    val content: String,
    val language: String?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * A math (TeX) block.
 * @param expression expression content
 */
data class Math(
    val expression: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * A horizontal line (thematic break).
 */
class HorizontalRule : Node {
    override fun toString() = "HorizontalRule"

    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * A heading defined via prefix symbols.
 * @param depth importance (`depth=1` for H1, `depth=6` for H6)
 */
data class Heading(
    val depth: Int,
    override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * Creation of a link reference.
 * @param label label to be referenced
 * @param url associated URL
 * @param title optional reference title
 */
data class LinkDefinition(
    val label: InlineContent,
    val url: String,
    val title: String?,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Alias for [label].
     */
    override val text: InlineContent
        get() = label
}

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
) : ListBlock {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

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
) : ListBlock {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * An item of a [ListBlock].
 */
interface ListItem : NestableNode

/**
 * An item of a [ListBlock].
 * @param children content
 */
data class BaseListItem(
    override val children: List<Node>,
) : ListItem {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * An item of a [ListBlock] that includes a task, with a checked/unchecked value.
 * @param isChecked whether the task is checked
 * @param children content
 */
data class TaskListItem(
    val isChecked: Boolean,
    override val children: List<Node>,
) : ListItem {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * An HTML block.
 * @param content raw HTML content
 */
data class Html(
    val content: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * A table.
 * @param columns columns of the table. Each column has a header and multiple cells
 */
data class Table(
    val columns: List<Column>,
) : Node {
    /**
     * A column of a table.
     * @param alignment text alignment
     * @param header header cell
     * @param cells other cells
     */
    data class Column(val alignment: Alignment, val header: Cell, val cells: List<Cell>)

    /**
     * A single cell of a table.
     * @param text content
     */
    data class Cell(val text: InlineContent)

    /**
     * Text alignment of a [Column].
     */
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT,
        NONE,
    }

    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * A text paragraph.
 * @param text text content
 */
data class Paragraph(
    override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A block quote.
 * @param children content
 */
data class BlockQuote(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}

/**
 * Anything else (should not happen).
 */
class BlockText : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = TODO("Not yet implemented")
}
