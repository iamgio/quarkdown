package com.quarkdown.core.function.value

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.reflect.annotation.NoAutoArgumentUnwrapping
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * A [Value] whose type has not been yet determined.
 * - This is more commonly used as an [InputValue] to represent a value written by the user
 * that does not have a specific type yet.
 * - It is also used as an [OutputValue] by functions such as the stdlib `.function`, which
 * returns general content that can be used as any type, depending on the needs.
 * @param unwrappedValue either a raw/unprocessed representation of the wrapped value (e.g. the number 5 saved as the string "5")
 *                       or simply an opaque wrapper for a generic value (e.g. a `Node`)
 * @param evaluationContext optional context that preserves the scope in which this value was produced.
 *                          When set (e.g. by a lambda parameter function), the output visitor uses this context
 *                          instead of its own to parse raw string content, ensuring that deferred variable references
 *                          resolve in the correct scope.
 * @see com.quarkdown.core.function.reflect.DynamicValueConverter
 */
@NoAutoArgumentUnwrapping
data class DynamicValue(
    override val unwrappedValue: Any?,
    val evaluationContext: Context? = null,
) : InputValue<Any?>,
    OutputValue<Any?> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
