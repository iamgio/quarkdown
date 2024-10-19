package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A comment whose content is ignored.
 */
object Comment : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
