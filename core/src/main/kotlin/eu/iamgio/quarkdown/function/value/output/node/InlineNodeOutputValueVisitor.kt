package eu.iamgio.quarkdown.function.value.output.node

import eu.iamgio.quarkdown.ast.base.inline.CheckBox
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.factory.ValueFactory

/**
 * Producer of inline nodes from function output values.
 * @param context context of the function
 * @see NodeOutputValueVisitor
 */
class InlineNodeOutputValueVisitor(private val context: Context) : NodeOutputValueVisitor() {
    override fun visit(value: StringValue) = Text(value.unwrappedValue)

    override fun visit(value: NumberValue) = Text(value.unwrappedValue.toString())

    override fun visit(value: BooleanValue) = CheckBox(isChecked = value.unwrappedValue)

    override fun visit(value: ObjectValue<*>) = Text(value.unwrappedValue.toString())

    // Raw Markdown code is parsed as inline.
    override fun parseRaw(raw: String) = ValueFactory.inlineMarkdown(raw, context).asNodeValue()
}
