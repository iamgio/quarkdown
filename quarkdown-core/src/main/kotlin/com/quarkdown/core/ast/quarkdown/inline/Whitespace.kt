package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.ast.Node
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An empty square that adds whitespace to the layout.
 * If both width and height are `null`, the whitespace consists of a blank space.
 * @param width width of the whitespace
 * @param height height of the whitespace
 * @param isBlock whether the whitespace is a block element (like a paragraph) or inline (like a text span)
 */
class Whitespace(
    val width: Size?,
    val height: Size?,
    @Diverge val isBlock: Boolean = false,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
