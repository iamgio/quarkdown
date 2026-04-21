package com.quarkdown.core.rendering

import com.quarkdown.core.ast.attributes.link.getResolvedUrl
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.media.getStoredMedia
import com.quarkdown.core.context.Context
import com.quarkdown.core.media.passthrough.MediaPassthrough
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A node rendering visitor, which converts nodes from the AST to their output code representation.
 */
abstract class NodeRenderer(
    protected val context: Context,
) : NodeVisitor<CharSequence> {
    private fun replaceMediaPassthroughPrefix(url: String): String =
        MediaPassthrough.replacePassthroughPrefix(
            url,
            createMediaPassthroughPrefixReplacement(),
        )

    /**
     * Creates the string to replace the [MediaPassthrough] prefix with when encountered in a link or image URL.
     * For example, when rendering the `@/image.jpg` URL in HTML, `@`, the passthrough prefix,
     * is replaced with the relative path to the output directory root.
     */
    abstract fun createMediaPassthroughPrefixReplacement(): String

    /**
     * Visits a transformed [Link] node, which has its URL already resolved and media passthrough prefix replaced if applicable.
     */
    abstract fun visitTransformed(node: Link): CharSequence

    /**
     * Visits a transformed [Image] node, which has its link's URL already resolved and media passthrough prefix replaced if applicable.
     */
    abstract fun visitTransformed(node: Image): CharSequence

    final override fun visit(node: Link): CharSequence {
        val url: String = node.getResolvedUrl(context).let(::replaceMediaPassthroughPrefix)
        val link = node.copy(url = url)
        return visitTransformed(link)
    }

    /**
     * Visits the link definition of the reference, or falls back to visiting the reference label if resolution fails.
     */
    final override fun visit(node: ReferenceLink): CharSequence = (node.getDefinition(context) ?: node.fallback()).accept(this)

    final override fun visit(node: Image): CharSequence {
        val url: String =
            node.link.getStoredMedia(context)?.path
                ?: node.link.getResolvedUrl(context).let(::replaceMediaPassthroughPrefix)

        val image = node.copy(link = node.link.copy(url = url))
        return visitTransformed(image)
    }
}
