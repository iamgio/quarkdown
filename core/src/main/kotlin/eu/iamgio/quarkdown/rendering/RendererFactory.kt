package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.rendering.html.HtmlNodeRenderer
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Static provider of rendering strategies.
 */
object RendererFactory {
    /**
     * @param attributes additional attributes from the parsing stage
     * @return a new HTML node renderer
     */
    fun html(attributes: AstAttributes): NodeVisitor<CharSequence> = HtmlNodeRenderer(attributes)
}
