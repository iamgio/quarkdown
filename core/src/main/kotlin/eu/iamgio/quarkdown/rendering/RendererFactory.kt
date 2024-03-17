package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.rendering.html.HtmlNodeRenderer

/**
 * Static provider of rendering strategies.
 */
object RendererFactory {
    /**
     * @return a new HTML node renderer
     */
    fun html(): NodeVisitor<CharSequence> = HtmlNodeRenderer()
}
