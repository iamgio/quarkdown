package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.SectionLocation
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Node that can be numbered depending on its location in the document
 * and the amount of occurrences according to its [key].
 * @param key name to group (and count) numbered nodes
 * @param children supplier of the node content given the evaluated [SectionLocation], formatted according to the active [DocumentNumbering]
 * @see com.quarkdown.core.context.hooks.location.LocationAwareLabelStorerHook for storing locations
 * @see com.quarkdown.core.document.numbering.NumberingFormat
 */
class Numbered(
    val key: String,
    val children: (location: String) -> List<Node>,
) : LocationTrackableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
