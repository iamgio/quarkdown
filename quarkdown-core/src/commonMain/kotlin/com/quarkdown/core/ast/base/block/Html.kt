package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An HTML block.
 * @param content raw HTML content
 */
class Html(
    val content: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
