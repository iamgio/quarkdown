package com.quarkdown.core.ast.quarkdown

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A call to a function.
 * The call is executed after parsing, and its output is stored in its mutable [children].
 * @param context context this node lies in, which is where symbols will be loaded from upon execution
 * @param name name of the function to call
 * @param arguments arguments to call the function with
 * @param isBlock whether this function call is an isolated block (opposite: inline)
 * @param sourceText if available, the source code of the whole function call
 * @param sourceRange if available, the range of the function call in the source code
 */
class FunctionCallNode(
    val context: Context,
    val name: String,
    val arguments: List<FunctionCallArgument>,
    val isBlock: Boolean,
    val sourceText: CharSequence? = null,
    val sourceRange: IntRange? = null,
) : NestableNode {
    override val children: MutableList<Node> = mutableListOf()

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
