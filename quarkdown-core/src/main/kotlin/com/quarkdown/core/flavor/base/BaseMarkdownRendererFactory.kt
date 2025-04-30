package com.quarkdown.core.flavor.base

import com.quarkdown.core.context.Context
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.core.rendering.html.BaseHtmlNodeRenderer
import com.quarkdown.core.rendering.html.HtmlPostRenderer

/**
 * [BaseMarkdownFlavor] renderer factory.
 */
class BaseMarkdownRendererFactory : RendererFactory {
    override fun html(context: Context) =
        RenderingComponents(
            nodeRenderer = BaseHtmlNodeRenderer(context),
            postRenderer = HtmlPostRenderer(context),
        )
}
