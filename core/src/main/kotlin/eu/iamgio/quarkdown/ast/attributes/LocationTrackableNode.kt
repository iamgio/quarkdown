package eu.iamgio.quarkdown.ast.attributes

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context

/**
 * A node that requests its location to be tracked within the document.
 * By location, it is meant the section indices ([SectionLocation]) the node is located in.
 * @see SectionLocation
 */
interface LocationTrackableNode : Node

/**
 * The location of a node within the document, in terms of section indices.
 * Example:
 * ```markdown
 * # A
 * ## A.A
 * # B
 * ## B.A
 * Node <-- location: B.A, represented by [1, 0]
 * ```
 * @param levels section indices
 */
data class SectionLocation(val levels: List<Int>)

/**
 * @return the location of this node within the document handled by [context],
 * or `null` if the location for [this] node is not registered
 */
fun LocationTrackableNode.getLocation(context: Context): SectionLocation? = context.attributes.locations[this]
