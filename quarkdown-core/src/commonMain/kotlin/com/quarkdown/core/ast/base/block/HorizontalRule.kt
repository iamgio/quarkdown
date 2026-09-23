package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A horizontal line (thematic break).
 */
object HorizontalRule : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
