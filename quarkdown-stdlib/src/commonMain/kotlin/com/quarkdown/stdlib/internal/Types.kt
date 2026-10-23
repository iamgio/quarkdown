package com.quarkdown.stdlib.internal

import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.Value

// Types utilities for the stdlib.

/**
 * Converts [Value] to a [String].
 */
internal fun Value<*>.asString(): String =
    when (val value = unwrappedValue) {
        is Value<*> -> value.asString()
        else -> value.toString()
    }

/**
 * Converts [Value] to a [Double].
 * @return the value as a double, or 0 if the value is not numeric
 */
internal fun Value<*>.asDouble(): Double =
    when (val value = unwrappedValue) {
        is Number -> value.toDouble()
        else -> value.toString().toDoubleOrNull()
    } ?: .0

/**
 * Converts [Value] to a [String], flattening nested collections.
 * A collection is represented by its elements, recursively converted and joined by [separator].
 * Any other value is converted via [asString].
 * @param separator string placed between adjacent elements of a collection
 * @return the value as a string
 */
internal fun Value<*>.asJoinedString(separator: String): String =
    when (this) {
        is IterableValue<*> -> joinToString(separator) { it.asJoinedString(separator) }
        else -> asString()
    }
