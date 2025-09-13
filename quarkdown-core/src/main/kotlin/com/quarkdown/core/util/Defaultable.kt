package com.quarkdown.core.util

/**
 * Marker interface for types that support default values.
 */
interface Defaultable

/**
 * Holds a value and an optional default value of type [T].
 * @param value the primary value
 * @param default the default value, if any
 */
data class Defaulted<T : Defaultable>(
    val value: T,
    val default: T? = null,
)

/**
 * Wraps a value with an optional default.
 * @param default the default value to associate
 * @return a [Defaulted] instance containing the value and default
 */
fun <T : Defaultable> T.withDefault(default: T?): Defaulted<T> = Defaulted(this, default)

/**
 * Retrieves a value using [valueProvider] from the primary value,
 * or from the default if the primary value is `null`.
 * @param valueProvider function to extract a value from [T]
 * @return the provided value, or the default if not present. `null` if neither is present
 */
operator fun <T : Defaultable, V> Defaulted<T>.get(valueProvider: T.() -> V?): V? = valueProvider(value) ?: default?.valueProvider()
