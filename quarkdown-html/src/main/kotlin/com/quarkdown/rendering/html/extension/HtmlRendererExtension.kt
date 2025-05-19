package com.quarkdown.rendering.html.extension

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.rendering.html.post.HtmlPostRenderer

/**
 * Entry point of the HTML rendering extension.
 */
fun RendererFactory.html(context: Context) =
    RenderingComponents(
        nodeRenderer = accept(HtmlRendererFactoryVisitor(context)),
        postRenderer = HtmlPostRenderer(context),
    )
