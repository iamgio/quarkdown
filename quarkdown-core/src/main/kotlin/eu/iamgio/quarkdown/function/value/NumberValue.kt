package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor
import kotlin.math.ceil
import kotlin.math.floor

/**
 * An immutable numeric [Value].
 */
data class NumberValue(private val rawUnwrappedValue: Number) : InputValue<Number>, OutputValue<Number> {
    /**
     * [rawUnwrappedValue] adapted to either [Int] or [Float] depending on its value.
     */
    override val unwrappedValue: Number =
        rawUnwrappedValue.let {
            when {
                it is Int || it is Long -> it // 5 -> 5
                ceil(it.toFloat()) == floor(it.toFloat()) -> it.toInt() // 5.0 -> 5
                else -> it // 5.2 -> 5.2
            }
        }

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] number wrapped into a [NumberValue]
 */
fun Number.wrappedAsValue() = NumberValue(this)
