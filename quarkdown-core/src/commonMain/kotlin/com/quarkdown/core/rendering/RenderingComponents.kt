package com.quarkdown.core.rendering

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
)
