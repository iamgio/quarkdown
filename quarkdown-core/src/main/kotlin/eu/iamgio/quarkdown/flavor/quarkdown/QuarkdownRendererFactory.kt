package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.rendering.RenderingComponents
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer
import eu.iamgio.quarkdown.rendering.html.QuarkdownHtmlNodeRenderer

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
