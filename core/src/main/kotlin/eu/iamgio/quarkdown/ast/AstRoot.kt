package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * The AST root.
 */
data class AstRoot(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

typealias Document = AstRoot

// Used in function libraries (e.g. stdlib).
typealias MarkdownContent = AstRoot
