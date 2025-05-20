package com.quarkdown.core.property

/**
 * A property is an atom that represents a value associated with a key, similar to a map entry.
 *
 * Example implementation:
 * ```kotlin
 * data class MyProperty(override val value: X) : Property<X> {
 *     companion object : Property.Key<X>
 *
 *     override val key = MyProperty
 * }
 * ```
 *
 * @param T the type of the value associated with the key
 * @see PropertyContainer
 */
interface Property<T> {
    /**
     * The key of the property, used to identify it.
     */
    val key: Key<T>

    /**
     * The value of the property.
     */
    val value: T

    /**
     * A key type for a [Property].
     * @param T the type of property that this key can be associated with
     */
    interface Key<T>
}
