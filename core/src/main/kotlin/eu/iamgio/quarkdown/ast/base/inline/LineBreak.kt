package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A hard line break.
 */
class LineBreak : Node {
    override fun toString() = "LineBreak"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
