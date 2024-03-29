package eu.iamgio.quarkdown.flavor

import eu.iamgio.quarkdown.ast.context.Context
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Provider of rendering strategies.
 */
interface RendererFactory {
    /**
     * @param context additional information gathered during the parsing stage
     * @return a new HTML node renderer
     */
    fun html(context: Context): NodeVisitor<CharSequence>
}
