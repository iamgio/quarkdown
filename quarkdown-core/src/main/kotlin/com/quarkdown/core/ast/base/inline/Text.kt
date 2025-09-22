package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A [Node] that contains plain text.
 * @see com.quarkdown.core.util.toPlainText
 */
interface PlainTextNode : Node {
    val text: String
}

/**
 * Content (usually a single character) that requires special treatment during the rendering stage.
 * @param text wrapped text
 */
class CriticalContent(
    override val text: String,
) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Plain inline text.
 * @param text text content.
 */
class Text(
    override val text: String,
) : PlainTextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
