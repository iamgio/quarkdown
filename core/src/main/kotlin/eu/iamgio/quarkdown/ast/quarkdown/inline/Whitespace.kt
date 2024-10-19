package eu.iamgio.quarkdown.ast.quarkdown.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * An empty square that adds whitespace to the layout.
 * If both width and height are `null`, the whitespace consists of a blank space.
 * @param width width of the whitespace
 * @param height height of the whitespace
 */
class Whitespace(val width: Size?, val height: Size?) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
