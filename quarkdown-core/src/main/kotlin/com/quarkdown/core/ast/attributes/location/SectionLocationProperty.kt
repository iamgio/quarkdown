package com.quarkdown.core.ast.attributes.location

import com.quarkdown.core.property.Property

/**
 * [Property] that is assigned to each node that requests its location to be tracked ([LocationTrackableNode]).
 * It contains the node's location in the document, in terms of section indices.
 * @see SectionLocation
 * @see com.quarkdown.core.context.hooks.LocationAwarenessHook for the storing stage
 */
data class SectionLocationProperty(
    override val value: SectionLocation,
) : Property<SectionLocation> {
    companion object : Property.Key<SectionLocation>

    override val key: Property.Key<SectionLocation> = SectionLocationProperty
}
