package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.rendering.html.QuarkdownHtmlNodeRenderer

/**
 * [QuarkdownRendererFactory] renderer factory.
 */
class QuarkdownRendererFactory : RendererFactory {
    override fun html(context: Context) = QuarkdownHtmlNodeRenderer(context)
}
