package com.quarkdown.core.function.value

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * An immutable [Value] that contains other values of the same type, unordered.
 * @param T the element type of the list
 */
data class UnorderedCollectionValue<T : OutputValue<*>>(
    override val unwrappedValue: Set<T>,
) : IterableValue<T> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] set wrapped into a [UnorderedCollectionValue]
 */
fun <T : OutputValue<*>> Set<T>.wrappedAsValue() = UnorderedCollectionValue(this)
