package com.quarkdown.core.function.value.output.node

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.BlankNode
import com.quarkdown.core.ast.base.block.Newline
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.context.Context
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.util.trimBlankLines

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

    /**
     * The nodes that actually carry content within [this] node.
     * A successful function call is replaced by the content of its output,
     * while a failed one is kept as is, so that its error is not lost.
     */
    private fun Node.contentNodes(): List<Node> =
        when {
            this is BlankNode || this is Newline -> emptyList()
            this is FunctionCallNode && error == null -> children.flatMap { it.contentNodes() }
            else -> listOf(this)
        }

    // A NodeValue that wraps a plain block Paragraph, typically produced by an eval, is unwrapped into its inline children.
    override fun visit(value: NodeValue) =
        when (val node = value.unwrappedValue) {
            is Paragraph -> {
                InlineMarkdownContent(node.text)
            }

            is MarkdownContent -> {
                (node.children.flatMap { it.contentNodes() }.singleOrNull() as? Paragraph)
                    ?.let { InlineMarkdownContent(it.text) }
                    ?: node
            }

            else -> {
                node
            }
        }

    override fun parseRaw(
        raw: String,
        context: Context?,
    ) = ValueFactory.inlineMarkdown(raw.trimBlankLines(), context ?: this.context).asNodeValue()
}
