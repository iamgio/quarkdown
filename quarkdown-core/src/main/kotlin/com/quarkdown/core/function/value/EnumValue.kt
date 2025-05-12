package com.quarkdown.core.function.value

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor

/**
 * A [Value] that wraps an element from a static enum class.
 */
data class EnumValue(
    override val unwrappedValue: Enum<*>,
) : InputValue<Enum<*>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}

/**
 * @return [this] enum wrapped into an [EnumValue]
 */
fun Enum<*>.wrappedAsValue() = EnumValue(this)
