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
 * @param context context where location data is stored
 * @return the location of this node within the document handled by [context],
 * or `null` if the location for [this] node is not registered
 */
fun LocationTrackableNode.getLocation(context: Context): SectionLocation? = context.attributes.of(this)[SectionLocationProperty]

/**
 * Registers the location of this node within the document handled by [context].
 * @param context context where location data is stored
 * @param location location to set
 * @see com.quarkdown.core.context.hooks.location.LocationAwarenessHook
 */
fun LocationTrackableNode.setLocation(
    context: MutableContext,
    location: SectionLocation,
) {
    context.attributes.of(this) += SectionLocationProperty(location)
}

/**
 * @param context context where location data is stored
 * @return the location of this node within the document handled by [context],
 * formatted according to its corresponding [NumberingFormat] via [formatLocation].
 * Returns `null` if the location for [this] node is not registered or if it does not have a corresponding [NumberingFormat] rule
 */
fun LocationTrackableNode.getLocationLabel(context: Context): String? = context.attributes.of(this)[LocationLabelProperty]

/**
 * Registers the formatted location of this node within the document handled by [context],
 * according to [this] node's [NumberingFormat].
 * @param context context where location data is stored
 * @param label formatted location to set
 * @see com.quarkdown.core.context.hooks.location.LocationAwareLabelStorerHook
 */
fun LocationTrackableNode.setLocationLabel(
    context: MutableContext,
    label: String,
) {
    context.attributes.of(this) += LocationLabelProperty(label)
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
