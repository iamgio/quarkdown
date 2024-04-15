package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A call to a function.
 * The call is executed after parsing, and its output is stored in its mutable [children].
 * @param name name of the function to call
 * @param arguments arguments to call the function with
 * @param isBlock whether this function call is an isolated block (opposite: inline)
 */
data class FunctionCallNode(
    val name: String,
    val arguments: List<FunctionCallArgument>,
    val isBlock: Boolean,
) : NestableNode {
    override val children: MutableList<Node> = mutableListOf()

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
