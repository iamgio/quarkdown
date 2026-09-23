package com.quarkdown.core.ast

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A generic grouping of nodes that behaves like a sub-root.
 */
class AstGroup(
    @Diverge override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
