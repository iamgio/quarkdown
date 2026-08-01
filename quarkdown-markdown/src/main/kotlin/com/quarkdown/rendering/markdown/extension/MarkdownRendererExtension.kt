package com.quarkdown.rendering.markdown.extension

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.markdown.node.GfmNodeRenderer
import com.quarkdown.rendering.markdown.post.GfmPostRenderer

/**
 * The GitHub Flavored Markdown rendering plug-in produces a GFM representation of the document.
 * It can be used to export a Quarkdown document back to portable Markdown, suitable
 * for consumption by other Markdown-aware tools.
 */
@Suppress("UnusedReceiverParameter")
fun RendererFactory.gfm(context: Context) =
    RenderingComponents(
        nodeRenderer = GfmNodeRenderer(context),
        postRenderer = GfmPostRenderer(context),
    )
