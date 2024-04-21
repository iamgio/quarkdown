package eu.iamgio.quarkdown.function.value.output

import eu.iamgio.quarkdown.ast.BaseListItem
import eu.iamgio.quarkdown.ast.BlockText
import eu.iamgio.quarkdown.ast.CheckBox
import eu.iamgio.quarkdown.ast.ListItem
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.OrderedList
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.ast.UnorderedList
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.ListValue
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.SetValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.ValueFactory
import eu.iamgio.quarkdown.function.value.VoidValue
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Producer of a [Node] output, ready to append to the AST, from a generic function output.
 * @see eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
 */
class NodeOutputValueVisitor(private val context: Context) : OutputValueVisitor<Node> {
    override fun visit(value: StringValue) = Text(value.unwrappedValue)

    override fun visit(value: NumberValue) =
        value.unwrappedValue.let {
            when {
                it is Int || it is Long -> Text(it.toString()) // 5 -> 5
                ceil(it.toFloat()) == floor(it.toFloat()) -> Text(it.toInt().toString()) // 5.0 -> 5
                else -> Text(it.toString()) // 5.2 -> 5.2
            }
        }

    override fun visit(value: BooleanValue) = CheckBox(isChecked = value.unwrappedValue)

    private fun createListItems(value: IterableValue<*>): List<ListItem> =
        value.unwrappedValue.map {
            BaseListItem(
                listOf(
                    // Each item is represented by its own Node output.
                    it.accept(this),
                ),
            )
        }

    override fun visit(value: ListValue<*>) =
        OrderedList(
            startIndex = 1,
            isLoose = false,
            children = createListItems(value),
        )

    override fun visit(value: SetValue<*>) =
        UnorderedList(
            isLoose = false,
            children = createListItems(value),
        )

    override fun visit(value: NodeValue) = value.unwrappedValue

    override fun visit(value: VoidValue) = BlockText()

    // Dynamic output (e.g. produced by the stdlib function `.function`) is treated as Markdown by default.
    override fun visit(value: DynamicValue) =
        this.visit(
            ValueFactory.markdown(value.unwrappedValue, context).asNodeValue(),
        )
}
