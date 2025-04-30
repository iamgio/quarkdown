package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A comment whose content is ignored.
 */
object Comment : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
