package com.quarkdown.core.ast.attributes.link

import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.property.Property

/**
 * [Property] that is assigned to each image that points to a local relative path that is different from the original.
 * For instance, an image may have a link to `images/picture.png`,
 * but if it's loaded from an included document with a different base path, it may be resolved to, for example, `../images/picture.png`.
 * @see com.quarkdown.core.ast.base.inline.Link
 * @see com.quarkdown.core.context.hooks.location.Imag for the storing stage
 */
data class ResolvedImagePathProperty(
    override val value: String,
) : Property<String> {
    companion object : Property.Key<String>

    override val key = ResolvedImagePathProperty
}

/**
 * @param context context where resolution data is stored
 * @return the resolved URL of this node within the document handled by [context],
 * or the regular URL if a resolved one is not registered
 */
fun Image.getResolvedUrl(context: Context): String =
    context.attributes.of(this)[ResolvedImagePathProperty]
        ?: this.link.url

/**
 * Registers the resolved path of this node within the document handled by [context].
 * @param context context where resolution data is stored
 * @param resolvedUrl resolved URL to set
 * @see com.quarkdown.core.context.hooks.ImagePathResolverHook
 */
fun Image.setResolvedUrl(
    context: MutableContext,
    resolvedUrl: String,
) {
    context.attributes.of(this) += ResolvedImagePathProperty(resolvedUrl)
}
