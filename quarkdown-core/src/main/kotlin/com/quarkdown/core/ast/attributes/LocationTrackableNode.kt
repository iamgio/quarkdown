package com.quarkdown.core.ast.attributes

import com.quarkdown.core.ast.Node
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.document.numbering.NumberingFormat

/**
 * A node that requests its location to be tracked within the document's hierarchy.
 * By location, it is meant the section indices ([SectionLocation]) the node is located in.
 * @see SectionLocation
 */
interface LocationTrackableNode : Node {
    /**
     * Whether this node should be tracked in the document's hierarchy.
     */
    val canTrackLocation: Boolean
        get() = true
}

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
 * @see com.quarkdown.core.document.numbering.NumberingFormat
 * @see com.quarkdown.core.document.DocumentInfo.numberingOrDefault
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
