package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A forced page break.
 */
class PageBreak : Node {
    override fun toString() = "PageBreak"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
