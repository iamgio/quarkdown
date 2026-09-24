package com.quarkdown.core.ast

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * The root of a node tree.
 */
class AstRoot(
    @Diverge override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

typealias Document = AstRoot
