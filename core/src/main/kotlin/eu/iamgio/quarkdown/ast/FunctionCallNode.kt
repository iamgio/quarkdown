package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A call to a function.
 * The call is executed after parsing, and its output is stored in its mutable [children].
 * @param name name of the function to call
 * @param arguments arguments to call the function with
 */
data class FunctionCallNode(
    val name: String,
    val arguments: List<String>,
) : NestableNode {
    override val children: MutableList<Node> = mutableListOf()

    override fun <T> accept(visitor: NodeVisitor<T>): T = TODO("Not yet implemented")
}
