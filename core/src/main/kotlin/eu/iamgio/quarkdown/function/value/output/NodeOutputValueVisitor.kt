package eu.iamgio.quarkdown.function.value.output

import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.TaskListItem
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Producer of a [Node] output, ready to append to the AST, from a generic function output.
 * @see eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
 */
class NodeOutputValueVisitor : OutputValueVisitor<Node> {
    override fun visit(value: StringValue) = Text(value.unwrappedValue)

    override fun visit(value: NumberValue) =
        value.unwrappedValue.let {
            when {
                it is Int || it is Long -> Text(it.toString()) // 5 -> 5
                ceil(it.toFloat()) == floor(it.toFloat()) -> Text(it.toInt().toString()) // 5.0 -> 5
                else -> Text(it.toString()) // 5.2 -> 5.2
            }
        }

    // TODO create CheckBox node (TaskListItem creates an unwanted <li> tag)
    override fun visit(value: BooleanValue) = TaskListItem(isChecked = value.unwrappedValue, children = emptyList())

    override fun visit(value: NodeValue) = value.unwrappedValue

    override fun visit(value: VoidValue) = BlockText()
}
