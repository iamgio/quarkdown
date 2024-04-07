package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.reflect.KFunctionAdapter
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.output.NodeOutputValueVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * Given a [FunctionCallNode] from the AST, this expander resolves its referenced function, executes it
 * and maps its result to a visible output in the final document.
 * @param context context to retrieve the queued to-be-expanded function calls from
 * @param outputMapper producer of an AST output [Node] from the function call output
 */
class AstFunctionCallExpander(
    private val context: Context,
    private val outputMapper: OutputValueVisitor<Node> = NodeOutputValueVisitor(),
) {
    fun sum(
        a: Int,
        b: Int,
    ): NumberValue = NumberValue(a + b) // TODO test

    /**
     * Resolves, executes and stores the result of [node]'s referenced function.
     * @param node AST function call node to expand
     */
    private fun expand(node: FunctionCallNode) {
        val function = KFunctionAdapter(::sum) // TODO look up from name
        val call =
            FunctionCall(
                function,
                node.arguments.map { FunctionCallArgument(DynamicInputValue(it)) },
            )

        // The result of the function is converted into a node to be appended to the AST.
        val outputNode = call.execute().accept(this.outputMapper)
        node.children += outputNode
    }

    /**
     * Expands all unexpanded function calls present in [context].
     */
    fun expandAll() {
        context.functionCalls.forEach { expand(it) }
    }
}
