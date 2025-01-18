package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * Nothing. This is Quarkdown's equivalent of `null`.
 */
data object None

/**
 * A value that represents a missing value.
 */
data object NoneValue : InputValue<None>, OutputValue<None> {
    override val unwrappedValue = None

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
