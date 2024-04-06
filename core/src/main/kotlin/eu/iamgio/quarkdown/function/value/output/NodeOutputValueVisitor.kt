package eu.iamgio.quarkdown.function.value.output

import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue

/**
 * Producer of a [Node] output, ready to append to the AST, from a generic function output.
 * @see eu.iamgio.quarkdown.function.AstFunctionCallExpander
 */
class NodeOutputValueVisitor : OutputValueVisitor<Node> {
    override fun visit(value: StringValue) = Text(value.unwrappedValue)

    override fun visit(value: NumberValue) = Text(value.unwrappedValue.toString())

    override fun visit(value: NodeValue) = value.unwrappedValue

    override fun visit(value: VoidValue) = BlockText()
}
