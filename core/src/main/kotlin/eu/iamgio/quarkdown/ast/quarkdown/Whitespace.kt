package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * An empty square in a document that adds whitespace to the layout.
 */
data class Whitespace(val width: Size?, val height: Size?) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
