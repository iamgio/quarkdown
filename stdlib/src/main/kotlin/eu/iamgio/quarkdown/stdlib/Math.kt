package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.value.NumberValue

/**
 * @return arithmetic sum of [a] and [b]
 */
fun sum(
    a: Int,
    b: Int,
) = NumberValue(a + b)
