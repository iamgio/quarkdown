package com.quarkdown.core.ast.attributes.link

import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.property.Property

/**
 * [Property], assigned to each image link, that points to a local relative URL (path) that is different from the original.
 * For instance, an image may have a link to `images/picture.png`,
 * but if it's loaded from an included document with a different base path, it may be resolved to, for example, `../images/picture.png`.
 * @see com.quarkdown.core.ast.base.inline.Link
 * @see com.quarkdown.core.context.hooks.LinkUrlResolverHook for the storing stage
 */
data class ResolvedLinkUrlProperty(
    override val value: String,
) : Property<String> {
    companion object : Property.Key<String>

    override val key = ResolvedLinkUrlProperty
}

/**
 * @param context context where resolution data is stored
 * @return the resolved URL of this node within the document handled by [context],
 * or the regular URL if a resolved one is not registered
 */
fun LinkNode.getResolvedUrl(context: Context): String =
    context.attributes.of(this)[ResolvedLinkUrlProperty]
        ?: this.url

/**
 * Registers the resolved path of this node within the document handled by [context].
 * @param context context where resolution data is stored
 * @param resolvedUrl resolved URL to set
 * @see com.quarkdown.core.context.hooks.LinkUrlResolverHook
 */
fun LinkNode.setResolvedUrl(
    context: MutableContext,
    resolvedUrl: String,
) {
    context.attributes.of(this) += ResolvedLinkUrlProperty(resolvedUrl)
}
