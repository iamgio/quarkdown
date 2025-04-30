package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A forced page break.
 */
class PageBreak : Node {
    override fun toString() = "PageBreak"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
