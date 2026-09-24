package com.quarkdown.core.function.value

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * An immutable string [Value].
 */
data class StringValue(
    override val unwrappedValue: String,
) : InputValue<String>,
    OutputValue<String>,
    AdaptableValue<InlineMarkdownContentValue> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)

    // A string value can be passed in place of Markdown content to represent plain text.
    override fun adapt() =
        InlineMarkdownContentValue(
            InlineMarkdownContent(
                buildInline { text(unwrappedValue) },
            ),
        )
}

/**
 * @return [this] string wrapped into a [StringValue]
 */
fun String.wrappedAsValue() = StringValue(this)
