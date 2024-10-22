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
class UnorderedList(
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
class OrderedList(
    val startIndex: Int,
    override val isLoose: Boolean,
    override val children: List<Node>,
) : ListBlock {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An item of a [ListBlock].
 */
abstract class ListItem : NestableNode {
    /**
     * The list that owns this item.
     * This property is set by the parser and should not be externally modified.
     */
    var owner: ListBlock? = null
}

/**
 * A simple item of a [ListBlock].
 * @param isFocused (Quarkdown extension) whether the item is focused.
 *                  When at least one item is focused, the other items are rendered so that
 *                  the focused items are more visible.
 *                  This property has an effect only when using a Quarkdown renderer.
 * @param children content
 */
class BaseListItem(
    val isFocused: Boolean = false,
    override val children: List<Node>,
) : ListItem() {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An item of a [ListBlock] that includes a task, with a checked/unchecked value.
 * @param isChecked whether the task is checked
 * @param children content
 * @see eu.iamgio.quarkdown.ast.base.inline.CheckBox
 */
class TaskListItem(
    val isChecked: Boolean,
    override val children: List<Node>,
) : ListItem() {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
