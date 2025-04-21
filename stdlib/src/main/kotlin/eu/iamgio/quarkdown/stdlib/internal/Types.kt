package eu.iamgio.quarkdown.stdlib.internal

import eu.iamgio.quarkdown.function.value.Value

// Types utilities for the stdlib.

/**
 * Converts [Value] to a [Double].
 * @return the value as a double, or 0 if the value is not numeric
 */
fun Value<*>.asDouble(): Double =
    when (val value = unwrappedValue) {
        is Number -> value.toDouble()
        else -> value.toString().toDoubleOrNull()
    } ?: .0
