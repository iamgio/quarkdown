package com.quarkdown.core.flavor.quarkdown

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.core.rendering.html.HtmlPostRenderer
import com.quarkdown.core.rendering.html.QuarkdownHtmlNodeRenderer

/**
 * [QuarkdownRendererFactory] renderer factory.
 */
class QuarkdownRendererFactory : RendererFactory {
    override fun html(context: Context) =
        RenderingComponents(
            nodeRenderer = QuarkdownHtmlNodeRenderer(context),
            postRenderer = HtmlPostRenderer(context),
        )
}
