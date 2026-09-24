package com.quarkdown.core.ast.base.block.list

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An item of a [ListBlock]. A list item may be enhanced via [ListItemVariant]s.
 * @param variants additional functionalities and characteristics of this item. For example, this item may contain a checked/unchecked task.
 * @param rawContent the raw source content of this item, if available. This is used for value conversion to iterable and dictionary values.
 * @see com.quarkdown.core.util.node.conversion.list.MarkdownListConverter for list-to-value conversion that utilizes the raw content.
 */
class ListItem(
    val variants: List<ListItemVariant> = emptyList(),
    @Diverge override val children: List<Node>,
    val rawContent: String? = null,
) : NestableNode {
    /**
     * The list that owns this item.
     * This property is set by the parser and should not be externally modified.
     */
    var owner: ListBlock? = null

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
