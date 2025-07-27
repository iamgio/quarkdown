package com.quarkdown.core.rendering

import com.quarkdown.core.document.sub.Subdocument

/**
 * A pair of a node renderer and a post-renderer, provided by a [com.quarkdown.core.flavor.RendererFactory].
 * For example, an HTML node renderer converts a node to an HTML tag, and an HTML post-renderer wraps the rendered content in a template.
 * Hence, it's a good idea to pair them together to ensure consistency.
 * @param nodeRenderer renderer of nodes
 * @param postRenderer handler of the rendered content ([nodeRenderer]'s output)
 */
data class RenderingComponents(
    val nodeRenderer: NodeRenderer,
    val postRenderer: PostRenderer,
) {
    /**
     * Adapts this [RenderingComponents] to a specific [Subdocument].
     * - It returns the same [RenderingComponents] in case the subdocument is the root document.
     * - Otherwise, it returns a copy of this [RenderingComponents] with the post-renderer adapted to the subdocument.
     * @param subdocument the subdocument to adapt to
     */
    fun forSubdocument(subdocument: Subdocument): RenderingComponents =
        when (subdocument) {
            Subdocument.ROOT -> this
            else -> copy(postRenderer = postRenderer.getSubdocumentPostRenderer(subdocument))
        }
}
