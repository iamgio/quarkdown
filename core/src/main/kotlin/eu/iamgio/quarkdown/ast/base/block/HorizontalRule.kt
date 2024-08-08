package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A horizontal line (thematic break).
 */
class HorizontalRule : Node {
    override fun toString() = "HorizontalRule"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
