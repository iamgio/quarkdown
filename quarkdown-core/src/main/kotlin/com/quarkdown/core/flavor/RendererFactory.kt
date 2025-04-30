package com.quarkdown.core.flavor

import com.quarkdown.core.context.Context
import com.quarkdown.core.rendering.RenderingComponents

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
