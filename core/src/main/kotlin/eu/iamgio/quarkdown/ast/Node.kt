package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A node of the abstract syntax tree - can be either a block or an inline element.
 */
interface Node {
    /**
     * Accepts a visitor.
     * @param T output type of the visitor
     * @return output of the visit operation
     */
    fun <T> accept(visitor: NodeVisitor<T>): T
}

/**
 * A node that may contain nested nodes as children.
 */
interface NestableNode : Node {
    val children: List<Node>
}
