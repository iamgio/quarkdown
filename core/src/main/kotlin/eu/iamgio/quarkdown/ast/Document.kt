package eu.iamgio.quarkdown.ast

/**
 * The AST root.
 */
data class Document(
    override val children: List<Node>,
) : NestableNode
