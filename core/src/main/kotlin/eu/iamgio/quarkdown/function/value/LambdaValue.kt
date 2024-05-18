package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor

/**
 * A [Value] that wraps an action of variable parameter count.
 */
data class LambdaValue(
    override val unwrappedValue: Function<DynamicValue>,
) : InputValue<Function<DynamicValue>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}
