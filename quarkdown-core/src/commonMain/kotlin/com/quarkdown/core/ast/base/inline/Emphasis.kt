package com.quarkdown.core.ast.base.inline

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Weakly emphasized content.
 * @param text content
 */
class Emphasis(
    @Diverge override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Strongly emphasized content.
 * @param text content
 */
class Strong(
    @Diverge override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Heavily emphasized content.
 * @param text content
 */
class StrongEmphasis(
    @Diverge override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * Strikethrough content.
 * @param text content
 */
class Strikethrough(
    @Diverge override val text: InlineContent,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
