package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A node that, when rendered in a `Slides` environment,
 * is displayed when the user attempts to go to the next slide.
 * Multiple fragments in the same slide are shown in order on distinct user interactions.
 */
data class SlidesFragment(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
