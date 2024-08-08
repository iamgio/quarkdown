package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Anything else (should not happen).
 */
class BlockText : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
