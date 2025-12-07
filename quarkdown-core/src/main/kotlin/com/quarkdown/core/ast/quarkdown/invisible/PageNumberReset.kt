package com.quarkdown.core.ast.quarkdown.invisible

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Marker node used to reset the logical page number during rendering.
 * @param startFrom the next page number that should be displayed, must be >= 1
 */
class PageNumberReset(
    val startFrom: Int,
) : Node {
    init {
        require(startFrom >= 1) { "startFrom must be greater than or equal to 1" }
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
