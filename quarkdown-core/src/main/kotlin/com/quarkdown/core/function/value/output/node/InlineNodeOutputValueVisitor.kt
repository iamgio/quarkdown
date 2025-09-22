package com.quarkdown.core.function.value.output.node

import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.factory.ValueFactory

/**
 * Producer of inline nodes from function output values.
 * @param context context of the function
 * @see NodeOutputValueVisitor
 */
class InlineNodeOutputValueVisitor(
    private val context: Context,
) : NodeOutputValueVisitor() {
    override fun visit(value: StringValue) = Text(value.unwrappedValue)

    override fun visit(value: NumberValue) = Text(value.unwrappedValue.toString())

    override fun visit(value: BooleanValue) = CheckBox(isChecked = value.unwrappedValue)

    override fun visit(value: ObjectValue<*>) = Text(value.unwrappedValue.toString())

    override fun visit(value: NoneValue) = CodeSpan(value.unwrappedValue.toString())

    // Raw Markdown code is parsed as inline.
    override fun parseRaw(raw: String) = ValueFactory.inlineMarkdown(raw, context).asNodeValue()
}
