package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor

/**
 * A sub-AST that contains Markdown nodes. This is usually accepted in 'body' parameters.
 */
data class MarkdownContentValue(override val unwrappedValue: MarkdownContent) : InputValue<MarkdownContent> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    /**
     * @return this content as a [NodeValue], suitable for function outputs
     */
    fun asNodeValue(): NodeValue = NodeValue(unwrappedValue)
}

/**
 * @return [this] Markdown content wrapped into a [MarkdownContentValue]
 */
fun MarkdownContent.wrappedAsValue() = MarkdownContentValue(this)
