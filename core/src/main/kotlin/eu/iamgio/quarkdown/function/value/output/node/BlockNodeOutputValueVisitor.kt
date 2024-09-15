package eu.iamgio.quarkdown.function.value.output.node

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.factory.ValueFactory

/**
 * Producer of block nodes from function output values.
 * @param context context of the function
 * @see NodeOutputValueVisitor
 */
class BlockNodeOutputValueVisitor(private val context: Context) : NodeOutputValueVisitor() {
    // Proxy used to convert inline values to block values.
    private val inline = InlineNodeOutputValueVisitor(context)

    /**
     * @return [this] node wrapped in a [Paragraph] block
     */
    private fun Node.inParagraph() = Paragraph(listOf(this))

    // Inline-to-block conversion.

    override fun visit(value: StringValue) = inline.visit(value).inParagraph()

    override fun visit(value: NumberValue) = inline.visit(value).inParagraph()

    override fun visit(value: BooleanValue) = inline.visit(value).inParagraph()

    override fun visit(value: ObjectValue<*>) = inline.visit(value).inParagraph()

    // Raw Markdown code is parsed as blocks.
    override fun parseRaw(raw: String) = ValueFactory.blockMarkdown(raw, context).asNodeValue()
}
