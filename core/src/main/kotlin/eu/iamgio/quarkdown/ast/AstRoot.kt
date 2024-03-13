package eu.iamgio.quarkdown.ast

/**
 * The AST root.
 */
data class AstRoot(
    override val children: List<Node>,
) : NestableNode

typealias Document = AstRoot
