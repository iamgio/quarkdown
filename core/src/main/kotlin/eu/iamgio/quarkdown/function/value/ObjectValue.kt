package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor

/**
 * A [Value] that wraps an element from a static enum class.
 */
data class ObjectValue<T>(override val unwrappedValue: T) : InputValue<T> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}
