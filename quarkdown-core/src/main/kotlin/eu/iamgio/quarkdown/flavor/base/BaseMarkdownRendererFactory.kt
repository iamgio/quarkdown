package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.rendering.RenderingComponents
import eu.iamgio.quarkdown.rendering.html.BaseHtmlNodeRenderer
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer

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
