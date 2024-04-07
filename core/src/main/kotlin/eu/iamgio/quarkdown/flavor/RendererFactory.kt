package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.rendering.NodeRenderer

/**
 * Provider of rendering strategies.
 */
interface RendererFactory {
    /**
     * @param context additional information gathered during the parsing stage
     * @return a new HTML node renderer
     */
    fun html(context: Context): NodeRenderer
}
