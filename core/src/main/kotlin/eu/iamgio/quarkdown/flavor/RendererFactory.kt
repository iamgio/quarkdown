package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Provider of rendering strategies.
 */
interface RendererFactory {
    /**
     * @param attributes additional attributes from the parsing stage
     * @return a new HTML node renderer
     */
    fun html(attributes: AstAttributes): NodeVisitor<CharSequence>
}
