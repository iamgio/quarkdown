package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Any unknown node type (should not happen).
 */
object BlankNode : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
