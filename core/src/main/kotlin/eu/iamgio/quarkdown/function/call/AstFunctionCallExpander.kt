package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.error.InvalidFunctionCallException
import eu.iamgio.quarkdown.function.value.DynamicInputValue
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
    /**
     * Resolves, executes and stores the result of [node]'s referenced function.
     * @param node AST function call node to expand
     */
    private fun expand(node: FunctionCallNode) {
        val function = context.getFunctionByName(node.name)

        if (function == null) {
            // TODO better error handling
            node.children += Text("Unresolved function '${node.name}'")
            return
        }

        val call =
            FunctionCall(
                function,
                node.arguments.map { FunctionCallArgument(DynamicInputValue(it)) },
            )

        try {
            // The result of the function is converted into a node to be appended to the AST.
            val outputNode = call.execute().accept(this.outputMapper)
            node.children += outputNode
        } catch (e: InvalidFunctionCallException) {
            // If the function call is invalid.
            context.errorHandler.handle(e) { message ->
                node.children += Text(message) // Shows error message in the final document.
            }
        }
    }

    /**
     * Expands all unexpanded function calls present in [context].
     */
    fun expandAll() {
        context.functionCalls.forEach { expand(it) }
    }
}
