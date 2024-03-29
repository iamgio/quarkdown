package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.ast.context.Context
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.rendering.html.BaseHtmlNodeRenderer

/**
 * [QuarkdownRendererFactory] renderer factory.
 */
class QuarkdownRendererFactory : RendererFactory {
    override fun html(context: Context) = BaseHtmlNodeRenderer(context)
}
