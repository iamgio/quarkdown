package com.quarkdown.core.ast.base.block.list

/**
 * A list item variant that adds a checkbox, which can be checked or unchecked, to a [ListItem].
 * @param isChecked whether the item is checked
 */
data class TaskListItemVariant(val isChecked: Boolean) : ListItemVariant {
    override fun <T> accept(visitor: ListItemVariantVisitor<T>) = visitor.visit(this)
}
