package com.quarkdown.core.function.value.output.node

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NodeValue
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
    override fun visit(value: StringValue) = CriticalContent(value.unwrappedValue)

    override fun visit(value: NumberValue) = Text(value.unwrappedValue.toString())

    override fun visit(value: BooleanValue) = CheckBox(isChecked = value.unwrappedValue)

    override fun visit(value: ObjectValue<*>) = CriticalContent(value.unwrappedValue.toString())

    override fun visit(value: NoneValue) = CodeSpan(value.unwrappedValue.toString())

    // A NodeValue that wraps a plain block Paragraph, typically produced by an eval, is unwrapped into its inline children.
    override fun visit(value: NodeValue) =
        when (val node = value.unwrappedValue) {
            is Paragraph -> {
                InlineMarkdownContent(node.text)
            }

            is MarkdownContent -> {
                (node.children.singleOrNull() as? Paragraph)
                    ?.let { InlineMarkdownContent(it.text) }
                    ?: node
            }

            else -> {
                node
            }
        }

    // Raw Markdown code is parsed as inline.
    override fun parseRaw(
        raw: String,
        context: Context?,
    ) = ValueFactory.inlineMarkdown(raw, context ?: this.context).asNodeValue()
}
