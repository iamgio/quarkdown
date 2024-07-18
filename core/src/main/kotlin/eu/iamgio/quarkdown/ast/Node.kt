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

/**
 * A node that may contain inline content.
 */
interface TextNode : Node {
    /**
     * The text of the node as processed inline content.
     */
    val text: InlineContent
}

/**
 * A general link node.
 * @see Link
 * @see LinkDefinition
 */
interface LinkNode : Node {
    /**
     * Inline content of the displayed label.
     */
    val label: InlineContent

    /**
     * URL this link points to.
     */
    val url: String

    /**
     * Optional title.
     */
    val title: String?
}
