package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.data.Range
import kotlin.math.PI
import kotlin.math.pow

/**
 * `Math` stdlib module exporter.
 */
val Math: Module =
    setOf(
        ::sum,
        ::subtract,
        ::multiply,
        ::divide,
        ::rem,
        ::pow,
        ::pi,
        ::sin,
        ::cos,
        ::tan,
        ::isEven,
        ::range,
    )

// Basic operations

/**
 * @return arithmetic floating-point sum of [a] and [b]
 */
fun sum(
    a: Number,
    b: Number,
) = NumberValue(a.toFloat() + b.toFloat())

/**
 * @return arithmetic floating-point subtraction of [a] and [b]
 */
fun subtract(
    @Name("from") a: Number,
    b: Number,
) = NumberValue(a.toFloat() - b.toFloat())

/**
 * @return arithmetic floating-point multiplication of [a] and [b]
 */
fun multiply(
    a: Number,
    @Name("by") b: Number,
) = NumberValue(a.toFloat() * b.toFloat())

/**
 * @return arithmetic floating-point division of [a] and [b]
 */
fun divide(
    a: Number,
    @Name("by") b: Number,
) = NumberValue(a.toFloat() / b.toFloat())

/**
 * @return remainder of the arithmetic floating-point division of [a] and [b]
 */
fun rem(
    a: Number,
    b: Number,
) = NumberValue(a.toFloat() % b.toFloat())

/**
 * @param base base number
 * @param exponent exponent number. If it is a floating-point number, it will be truncated to an integer
 * @return [base] raised to the power of [exponent]
 */
fun pow(
    base: Number,
    @Name("to") exponent: Number,
) = NumberValue(base.toFloat().pow(exponent.toInt()))

// Trigonometry

/**
 * @return the value of pi
 */
fun pi() = NumberValue(PI)

/**
 * @return sine of the angle [x] given in radians.
 */
fun sin(x: Number) = NumberValue(kotlin.math.sin(x.toFloat()))

/**
 * @return cosine of the angle [x] given in radians.
 */
fun cos(x: Number) = NumberValue(kotlin.math.cos(x.toFloat()))

/**
 * @return tangent of the angle [x] given in radians.
 */
fun tan(x: Number) = NumberValue(kotlin.math.tan(x.toFloat()))

// Misc

/**
 * @return whether the integer value of [x] is an even number
 */
@Name("iseven")
fun isEven(x: Number) = BooleanValue(x.toInt() % 2 == 0)

/**
 * Creates a range of numbers, which can also be iterated through.
 * The behavior of an open range is delegated to the consumer.
 * For instance, using a left-open range with [forEach] will make the loop start from 1.
 * The difference between this function and the built-in `..` operator is that the latter
 * does not allow for dynamic evaluation, hence both ends must be literals.
 * This function allows evaluating ends dynamically: for instance, `.range from:{1} to:{.sum {1} {2}}`.
 * Floating-point numbers are truncated to integers.
 * @property start start of the range (inclusive). If `null`, the range is infinite on the left end
 * @property end end of the range (inclusive). If `null`, the range is infinite on the right end. [end] > [start]
 */
fun range(
    @Name("from") start: Number? = null,
    @Name("to") end: Number? = null,
): ObjectValue<Range> =
    ObjectValue(
        Range(start?.toInt(), end?.toInt()),
    )
