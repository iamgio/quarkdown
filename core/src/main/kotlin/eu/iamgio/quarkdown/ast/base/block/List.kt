package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

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
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
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
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An item of a [ListBlock].
 */
interface ListItem : NestableNode {
    /**
     * The list that owns this item.
     */
    var owner: ListBlock?
}

/**
 * An item of a [ListBlock].
 * @param children content
 */
data class BaseListItem(
    override val children: List<Node>,
) : ListItem {
    override var owner: ListBlock? = null

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An item of a [ListBlock] that includes a task, with a checked/unchecked value.
 * @param isChecked whether the task is checked
 * @param children content
 * @see eu.iamgio.quarkdown.ast.base.inline.CheckBox
 */
data class TaskListItem(
    val isChecked: Boolean,
    override val children: List<Node>,
) : ListItem {
    override var owner: ListBlock? = null

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
