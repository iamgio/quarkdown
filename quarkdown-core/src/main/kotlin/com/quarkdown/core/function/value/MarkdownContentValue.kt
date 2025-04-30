package com.quarkdown.core.function.value

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor

/**
 * A sub-AST that contains Markdown nodes. This is usually accepted in 'body' parameters.
 */
data class MarkdownContentValue(override val unwrappedValue: MarkdownContent) : InputValue<MarkdownContent> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    /**
     * @return this content as a [NodeValue], suitable for function outputs
     */
    fun asNodeValue(): NodeValue = NodeValue(unwrappedValue)

    /**
     * @return this Markdown content value to an [InlineMarkdownContent] value. Wrapped content is identical
     */
    fun asInline() = InlineMarkdownContentValue(InlineMarkdownContent(unwrappedValue.children))
}

/**
 * @return [this] Markdown content wrapped into a [MarkdownContentValue]
 */
fun MarkdownContent.wrappedAsValue() = MarkdownContentValue(this)

/**
 * A sub-AST that contains Markdown nodes. This is usually accepted in 'body' parameters.
 */
data class InlineMarkdownContentValue(override val unwrappedValue: InlineMarkdownContent) : InputValue<InlineMarkdownContent> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    /**
     * @return this content as a [NodeValue], suitable for function outputs
     */
    fun asNodeValue(): NodeValue = NodeValue(unwrappedValue)
}
