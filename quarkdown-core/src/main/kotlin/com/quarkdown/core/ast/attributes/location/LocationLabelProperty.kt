package com.quarkdown.core.ast.attributes.location

import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.property.Property

/**
 * [Property] that is assigned to each [LocationTrackableNode] with an associated [NumberingFormat].
 * Labels are assigned based on each node's location, formatted via its corresponding numbering format.
 * The labels are often displayed in a caption.
 *
 * Examples of these nodes are figures and tables. For instance, depending on the document's [NumberingFormat],
 * an element may be labeled as `1.1`, `1.2`, `1.3`, `2.1`, etc.
 * @param value the formatted label
 * @see com.quarkdown.core.context.hooks.LocationAwareLabelStorerHook for the storing stage
 */
data class LocationLabelProperty(
    override val value: String,
) : Property<String> {
    companion object : Property.Key<String>

    override val key = LocationLabelProperty
}
