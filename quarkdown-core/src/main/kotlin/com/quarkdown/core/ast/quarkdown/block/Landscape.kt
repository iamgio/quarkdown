package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Transposes content to landscape orientation by rotating it 90 degrees counter-clockwise
 * with respect to the page size.
 */
class Landscape(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
