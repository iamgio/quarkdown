package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.value.NumberValue

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
    a: Number,
    b: Number,
) = NumberValue(a.toFloat() - b.toFloat())

/**
 * @return arithmetic floating-point multiplication of [a] and [b]
 */
fun multiply(
    a: Number,
    b: Number,
) = NumberValue(a.toFloat() * b.toFloat())

/**
 * @return arithmetic floating-point division of [a] and [b]
 */
fun divide(
    a: Number,
    b: Number,
) = NumberValue(a.toFloat() / b.toFloat())

/**
 * @return remainder of the arithmetic integer division of [a] and [b]
 */
fun rem(
    a: Number,
    b: Number,
) = NumberValue(a.toFloat() % b.toFloat())
