package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.call.FunctionCallArgument
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A call to a function.
 * The call is executed after parsing, and its output is stored in its mutable [children].
 * @param context context this node lies in, which is where symbols will be loaded from upon execution
 * @param name name of the function to call
 * @param arguments arguments to call the function with
 * @param isBlock whether this function call is an isolated block (opposite: inline)
 */
data class FunctionCallNode(
    val context: Context,
    val name: String,
    val arguments: List<FunctionCallArgument>,
    val isBlock: Boolean,
) : NestableNode {
    override val children: MutableList<Node> = mutableListOf()

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
