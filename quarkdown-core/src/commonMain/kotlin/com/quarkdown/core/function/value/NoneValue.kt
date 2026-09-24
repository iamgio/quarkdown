package com.quarkdown.core.function.value

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.output.OutputValueVisitor

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

/**
 * Whether this value represents a missing/null value,
 * either as a direct [NoneValue] or as a value wrapping [None], [NoneValue], or `null`.
 */
fun Value<*>.isNone(): Boolean = this is NoneValue || unwrappedValue.let { it == null || it is None || it is NoneValue }
