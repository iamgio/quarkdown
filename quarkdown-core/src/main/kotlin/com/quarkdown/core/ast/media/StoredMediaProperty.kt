package com.quarkdown.core.ast.media

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.context.Context
import com.quarkdown.core.media.storage.StoredMedia
import com.quarkdown.core.property.Property

/**
 * Property that can be attached to a [Node] in [AstAttributes.properties]
 * to signal that the node is bound to a [StoredMedia] resolved by a [com.quarkdown.core.media.storage.ReadOnlyMediaStorage].
 * @param value the stored media
 * @see StoredMedia
 * @see com.quarkdown.core.media.storage.ReadOnlyMediaStorage
 * @see com.quarkdown.core.ast.attributes.AstAttributes.properties
 */
data class StoredMediaProperty(
    override val value: StoredMedia,
) : Property<StoredMedia> {
    companion object : Property.Key<StoredMedia>

    override val key: Property.Key<StoredMedia> = StoredMediaProperty
}

/**
 * Retrieves the stored media associated with [this] node, if any.
 * @param attributes the attributes to extract the properties from
 * @return the stored media associated with [this] node, if any
 */
internal fun Node.getStoredMedia(attributes: AstAttributes): StoredMedia? = attributes.of(this)[StoredMediaProperty]

/**
 * Retrieves the stored media associated with [this] node, if any.
 * @param context the context to extract the properties from
 * @return the stored media associated with [this] node, if any
 */
fun Node.getStoredMedia(context: Context): StoredMedia? = getStoredMedia(context.attributes)
