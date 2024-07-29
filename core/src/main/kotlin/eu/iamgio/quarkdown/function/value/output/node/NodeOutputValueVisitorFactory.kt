package eu.iamgio.quarkdown.function.value.output.node

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitorFactory

/**
 * A factory that produces [OutputValueVisitor]s that map function output values
 * into [Node]s that can be appended to the AST.
 * @param context current context
 */
class NodeOutputValueVisitorFactory(private val context: Context) : OutputValueVisitorFactory<Node> {
    /**
     * @return a visitor that maps the output of a block function call into a block [Node]
     */
    override fun block(): OutputValueVisitor<Node> = BlockNodeOutputValueVisitor(context)

    /**
     * @return a visitor that maps the output of an inline function call into an inline [Node]
     */
    override fun inline(): OutputValueVisitor<Node> = InlineNodeOutputValueVisitor(context)
}
