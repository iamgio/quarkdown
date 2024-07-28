package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.reflect.NoAutoArgumentUnwrapping
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * A [Value] whose type has not been yet determined.
 * - This is more commonly used as an [InputValue] to represent a value written by the user
 * that does not have a specific type yet.
 * - It is also used as an [OutputValue] by functions such as the stdlib `.function`, which
 * returns general content that can be used as any type, depending on the needs.
 * @param unwrappedValue either a raw/unprocessed representation of the wrapped value (e.g. the number 5 saved as the string "5")
 *                       or simply an opaque wrapper for a generic value (e.g. a `Node`)
 * @see eu.iamgio.quarkdown.function.reflect.DynamicValueConverter
 */
@NoAutoArgumentUnwrapping
data class DynamicValue(override val unwrappedValue: Any?) : InputValue<Any?>, OutputValue<Any?> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
