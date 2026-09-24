package com.quarkdown.core.function.expression

import com.quarkdown.core.function.error.internal.InvalidExpressionEvalException
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.factory.ValueFactory

/**
 * An [Expression] that, upon failed evaluation due to an [InvalidExpressionEvalException],
 * delegates the operation to a safe fallback expression.
 * @see ValueFactory.safeExpression
 */
class SafeExpression(
    val expression: Expression,
    fallback: () -> Expression,
) : Expression {
    private val lazyFallback by lazy(fallback)

    override fun <T> accept(visitor: ExpressionVisitor<T>): T =
        try {
            expression.accept(visitor)
        } catch (e: InvalidExpressionEvalException) {
            lazyFallback.accept(visitor)
        }
}
