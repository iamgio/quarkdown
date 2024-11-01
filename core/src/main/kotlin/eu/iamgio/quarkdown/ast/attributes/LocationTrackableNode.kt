package eu.iamgio.quarkdown.ast.attributes

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.numbering.DocumentNumbering
import eu.iamgio.quarkdown.document.numbering.NumberingFormat

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
 * Node <-- location: B.A, represented by the levels [2, 1]
 * ```
 * @param levels section indices
 */
data class SectionLocation(val levels: List<Int>)

/**
 * @return the location of this node within the document handled by [context],
 * or `null` if the location for [this] node is not registered
 */
fun LocationTrackableNode.getLocation(context: Context): SectionLocation? = context.attributes.locations[this]

/**
 * @return the location of this node within the document handled by [context],
 * formatted according to the document's numbering format.
 * Returns `null` if the location for [this] node is not registered,
 * or if the document does not have a numbering format
 * @param context context where location data is stored
 * @param format numbering format to apply in order to stringify the location
 * @see getLocation
 * @see eu.iamgio.quarkdown.document.numbering.NumberingFormat
 * @see eu.iamgio.quarkdown.document.DocumentInfo.numberingOrDefault
 */
fun LocationTrackableNode.formatLocation(
    context: Context,
    format: (DocumentNumbering) -> NumberingFormat?,
): String? =
    this.getLocation(context)?.let {
        context.documentInfo.numberingOrDefault
            ?.let(format)
            ?.format(it, allowMismatchingLength = false)
    }
