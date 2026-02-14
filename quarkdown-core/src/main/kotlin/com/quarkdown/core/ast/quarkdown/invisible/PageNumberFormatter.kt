package com.quarkdown.core.ast.quarkdown.invisible

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Marker node used to specify the format of page numbers during rendering.
 * @param format the format string for page numbers, e.g. "1", "i", "A".
 */
class PageNumberFormatter(
    val format: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
