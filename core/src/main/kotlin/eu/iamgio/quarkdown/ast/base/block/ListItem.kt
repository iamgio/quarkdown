package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * An item of a [ListBlock]. A list item may be enhanced via [ListItemVariant]s.
 * @param variants additional functionalities and characteristics of this item. For example, this item may contain a checked/unchecked task.
 * @param children content
 */
class ListItem(
    val variants: List<ListItemVariant> = emptyList(),
    override val children: List<Node>,
) : NestableNode {
    /**
     * The list that owns this item.
     * This property is set by the parser and should not be externally modified.
     */
    var owner: ListBlock? = null

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A variant of a [ListItem] that brings additional functionalities to it.
 */
interface ListItemVariant {
    /**
     * Accepts a [ListItemVariantVisitor].
     * @param visitor visitor to accept
     * @return result of the visit operation
     */
    fun <T> accept(visitor: ListItemVariantVisitor<T>): T
}

/**
 * Visitor of [ListItemVariant]s.
 * @param T return type of the visit operations
 */
interface ListItemVariantVisitor<T> {
    fun visit(flavor: TaskListItemVariant): T

    fun visit(flavor: FocusListItemVariant): T
}

/**
 * A list item variant that adds a checkbox, which can be checked or unchecked, to a [ListItem].
 * @param isChecked whether the item is checked
 */
data class TaskListItemVariant(val isChecked: Boolean) : ListItemVariant {
    override fun <T> accept(visitor: ListItemVariantVisitor<T>) = visitor.visit(this)
}

/**
 * A list item variant that adds focus to a [ListItem].
 * When at least one item in a list is focused, the other items are rendered so that the focused ones are more visible.
 * This property has an effect only when using a Quarkdown renderer.
 * @param isFocused whether the item is focused.
 */
data class FocusListItemVariant(val isFocused: Boolean) : ListItemVariant {
    override fun <T> accept(visitor: ListItemVariantVisitor<T>) = visitor.visit(this)
}
