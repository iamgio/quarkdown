package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable string [Value].
 */
data class StringValue(override val unwrappedValue: String) : InputValue<String>, OutputValue<String> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] string wrapped into a [StringValue]
 */
fun String.wrappedAsValue() = StringValue(this)
