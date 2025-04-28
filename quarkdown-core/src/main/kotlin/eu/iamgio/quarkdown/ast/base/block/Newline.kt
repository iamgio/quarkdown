package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A blank line.
 */
object Newline : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
