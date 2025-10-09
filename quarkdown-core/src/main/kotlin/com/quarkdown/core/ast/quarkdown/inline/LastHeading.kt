package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Node that displays the last heading encountered, of the given [depth], before the current position.
 * @param depth the depth of the last [com.quarkdown.core.ast.base.block.Heading] to match
 */
class LastHeading(
    val depth: Int,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
