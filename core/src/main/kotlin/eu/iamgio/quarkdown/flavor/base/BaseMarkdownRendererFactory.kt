package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.rendering.html.BaseHtmlNodeRenderer

/**
 * [BaseMarkdownFlavor] renderer factory.
 */
class BaseMarkdownRendererFactory : RendererFactory {
    override fun html(context: Context) = BaseHtmlNodeRenderer(context)
}
