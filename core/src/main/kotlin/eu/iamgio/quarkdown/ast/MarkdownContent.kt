package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

// Nodes that aren't parsed from the source Markdown input,
// but rather returned from and/or handled by Quarkdown functions.

/**
 * A generic group of block nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.ValueFactory.blockMarkdown
 */
data class MarkdownContent(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}

/**
 * A generic group of inline nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.ValueFactory.inlineMarkdown
 */
data class InlineMarkdownContent(
    override val children: InlineContent,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}
