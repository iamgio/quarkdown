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
 * A node that may contain a variable number of nested nodes as children.
 */
interface NestableNode : Node {
    val children: List<Node>
}

/**
 * A node that contains a single child node.
 * @param T type of the child node
 */
interface SingleChildNestableNode<T : Node> : NestableNode {
    /**
     * The single child node.
     */
    val child: T

    /**
     * A singleton list containing [child].
     */
    override val children: List<Node>
        get() = listOf(child)
}
