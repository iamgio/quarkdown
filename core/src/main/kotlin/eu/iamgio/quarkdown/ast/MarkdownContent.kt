package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

// Utility nodes that are used as input in Quarkdown functions to expect Markdown data as an argument.

/**
 * A generic group of block nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.factory.ValueFactory.blockMarkdown
 */
data class MarkdownContent(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}

/**
 * A generic group of inline nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.factory.ValueFactory.inlineMarkdown
 */
data class InlineMarkdownContent(
    override val children: InlineContent,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}
