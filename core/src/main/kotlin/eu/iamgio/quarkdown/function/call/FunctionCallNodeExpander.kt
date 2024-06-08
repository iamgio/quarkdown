package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.ast.Box
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Paragraph
import eu.iamgio.quarkdown.ast.PlainTextNode
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.value.output.NodeOutputValueVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor
import eu.iamgio.quarkdown.pipeline.error.PipelineException

/**
 * Given a [FunctionCallNode] from the AST, this expander resolves its referenced function, executes it
 * and maps its result to a visible output in the final document.
 * @param context context to retrieve and handle the queued to-be-expanded function calls from
 * @param outputMapper producer of an AST output [Node] from the function call output
 */
class FunctionCallNodeExpander(
    private val context: MutableContext,
    private val outputMapper: OutputValueVisitor<Node> = NodeOutputValueVisitor(context),
) {
    /**
     * Resolves, executes and stores the result of [node]'s referenced function.
     * @param node AST function call node to expand
     */
    private fun expand(node: FunctionCallNode) {
        if (node.children.isNotEmpty()) {
            // The function call has already been expanded: do nothing.
            return
        }

        val call: UncheckedFunctionCall<*> = context.resolveUnchecked(node)

        try {
            // The result of the function is converted into a node to be appended to the AST.
            val outputNode = call.execute().accept(this.outputMapper)
            appendOutput(node, outputNode)
        } catch (e: PipelineException) {
            // If the function call is invalid.
            context.errorHandler.handle(e) { message ->
                appendOutput(node, Box.error(message)) // Shows an error message box in the final document.
            }
        }
    }

    /**
     * Adds [output] to [call]'s content in the AST.
     * If [call] is a block function call and [output] is inline, [output] is wrapped in a paragraph.
     * @param call function call node
     * @param output output node to append
     */
    private fun appendOutput(
        call: FunctionCallNode,
        output: Node,
    ) {
        call.children +=
            when {
                // The output is wrapped in a paragraph if the function call is a block and the output is inline.
                call.isBlock && output is PlainTextNode -> Paragraph(listOf(output))
                else -> output
            }
    }

    /**
     * Expands all unexpanded function calls present in [context], and empties queued function calls in [context].
     * This is performed on a copy of [Context.functionCalls] to avoid `ConcurrentModificationException`.
     * Hence, if a function call is added during the expansion, [expandAll] must be called again.
     */
    fun expandAll() {
        val calls = context.dequeueAllFunctionCalls()
        calls.forEach { expand(it) }
    }
}
