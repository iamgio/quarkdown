package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable numeric [Value].
 */
data class NumberValue(override val unwrappedValue: Number) : InputValue<Number>, OutputValue<Number> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] number wrapped into a [NumberValue]
 */
fun Number.wrappedAsValue() = NumberValue(this)
