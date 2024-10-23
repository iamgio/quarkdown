package eu.iamgio.quarkdown.ast.base.block.list

import eu.iamgio.quarkdown.ast.quarkdown.block.list.FocusListItemVariant

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
