package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A hard line break.
 */
object LineBreak : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
