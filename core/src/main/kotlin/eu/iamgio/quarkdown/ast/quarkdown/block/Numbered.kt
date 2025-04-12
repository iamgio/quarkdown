package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.attributes.SectionLocation
import eu.iamgio.quarkdown.document.numbering.DocumentNumbering
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Node that can be numbered depending on its location in the document
 * and the amount of occurrences according to its [key].
 * @param key name to group (and count) numbered nodes
 * @param children supplier of the node content given the evaluated [SectionLocation], formatted according to the active [DocumentNumbering]
 * @see eu.iamgio.quarkdown.context.hooks.LocationAwareLabelStorerHook for storing locations
 * @see eu.iamgio.quarkdown.document.numbering.NumberingFormat
 */
class Numbered(
    val key: String,
    val children: (location: String) -> List<Node>,
) : LocationTrackableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
