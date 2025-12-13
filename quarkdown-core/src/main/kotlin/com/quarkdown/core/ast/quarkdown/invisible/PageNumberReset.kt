package com.quarkdown.core.ast.quarkdown.invisible

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Marker node used to reset the logical page number during rendering.
 * @param startFrom the page number to start from after the reset
 */
class PageNumberReset(
    val startFrom: Int,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
