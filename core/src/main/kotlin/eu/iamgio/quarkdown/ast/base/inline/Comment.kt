package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A comment whose content is ignored.
 */
class Comment : Node {
    override fun toString() = "Comment"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
