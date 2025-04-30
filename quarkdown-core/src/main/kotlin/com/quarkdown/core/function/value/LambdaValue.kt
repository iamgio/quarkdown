package com.quarkdown.core.function.value

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.data.Lambda

/**
 * A [Value] that wraps an action of variable parameter count.
 */
data class LambdaValue(
    override val unwrappedValue: Lambda,
) : InputValue<Lambda> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}
