package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.rendering.RenderingComponents

/**
 * Provider of rendering strategies.
 */
interface RendererFactory {
    /**
     * @param context additional information gathered during the parsing stage
     * @return a new HTML node renderer and post-renderer
     */
    fun html(context: Context): RenderingComponents
}
