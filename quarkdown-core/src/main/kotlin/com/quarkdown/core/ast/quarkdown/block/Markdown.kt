package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A block of raw Markdown content that is emitted verbatim, without any additional processing or escaping,
 * only when the rendering target is Markdown.
 * @param content raw Markdown content
 */
class Markdown(
    val content: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
