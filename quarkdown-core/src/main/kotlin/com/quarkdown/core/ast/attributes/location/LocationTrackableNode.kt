package com.quarkdown.core.ast.attributes.location

import com.quarkdown.core.ast.Node
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
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
 * @return the location of this node within the document handled by [context],
 * or `null` if the location for [this] node is not registered
 */
fun LocationTrackableNode.getLocation(context: Context): SectionLocation? = context.attributes.of(this)[SectionLocationProperty]

/**
 * Registered the location of this node within the document handled by [context].
 * @param context context where location data is stored
 * @param location location to set
 */
fun LocationTrackableNode.setLocation(
    context: MutableContext,
    location: SectionLocation,
) {
    context.attributes.of(this) += SectionLocationProperty(location)
}

/**
 * @return the location of this node within the document handled by [context],
 * formatted according to the document's numbering format.
 * Returns `null` if the location for [this] node is not registered,
 * or if the document does not have a numbering format
 * @param context context where location data is stored
 * @param format numbering format to apply in order to stringify the location
 * @see getLocation
 * @see NumberingFormat
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
