package com.quarkdown.core.ast.base.block.list

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A list, either ordered or unordered.
 */
interface ListBlock : NestableNode {
    /**
     * Whether the list is loose.
     */
    val isLoose: Boolean

    /**
     * Items of the list.
     */
    val items: List<ListItem>
        get() = children.filterIsInstance<ListItem>()
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
