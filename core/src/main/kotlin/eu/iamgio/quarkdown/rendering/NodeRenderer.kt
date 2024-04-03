package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A rendering strategy, which converts nodes from the AST to their output code representation.
 */
interface NodeRenderer : NodeVisitor<CharSequence> {
    /**
     * Creates a new instance of a code wrapper for this rendering strategy.
     * A wrapper adds static content to the output code, and supports injection of values via placeholder keys, like a template file.
     * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
     * See `resources/render` for templates.
     * @return a new instance of the corresponding wrapper
     */
    fun createCodeWrapper(): RenderWrapper
}
