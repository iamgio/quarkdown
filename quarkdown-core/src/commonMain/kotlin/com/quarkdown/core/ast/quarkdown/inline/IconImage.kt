package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An icon node, which shows a pixel-perfect image from the icon library by its name.
 *
 * Note: icon libraries and names are dependent on the renderer.
 * No validation is performed by the compiler, and missing icons may not be rendered or rendered incorrectly.
 *
 * @param name the name of the icon
 */
class IconImage(
    val name: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
