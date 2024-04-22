package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NumberValue
import kotlin.math.PI

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
        ::pi,
        ::sin,
        ::cos,
        ::tan,
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
