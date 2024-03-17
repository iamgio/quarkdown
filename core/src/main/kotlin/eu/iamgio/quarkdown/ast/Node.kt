package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.rendering.NodeVisitor

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
 * A node that may contain nested tokens.
 */
interface NestableNode : Node {
    val children: List<Node>
}

/**
 * A node that may contain text.
 */
interface TextNode : Node {
    /**
     * The text of the node as processed inline content.
     */
    val text: InlineContent
}
