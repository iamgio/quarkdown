package com.quarkdown.core.function.value

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * A [Value] that wraps an element from a static enum class.
 */
data class ObjectValue<T>(
    override val unwrappedValue: T,
) : InputValue<T>,
    OutputValue<T> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
