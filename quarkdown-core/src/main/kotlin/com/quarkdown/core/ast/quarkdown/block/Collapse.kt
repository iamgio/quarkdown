package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A collapsible block, whose content can be hidden or shown by interacting with it.
 * @param title title of the block
 * @param isOpen whether the block is open at the beginning
 */
class Collapse(
    val title: InlineContent,
    val isOpen: Boolean,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
