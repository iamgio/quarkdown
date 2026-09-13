package com.quarkdown.core.property

import com.quarkdown.core.util.ConcurrentCache

/**
 * Associations between a key of type [T] and a [PropertyContainer].
 *
 * Example usage:
 * ```kotlin
 * val properties: AssociatedProperties<K> = ...
 * val key: K = ...
 * val value = properties.of(key)[MyProperty]
 * ```
 *
 * @param T the type of key elements
 * @param V the type of values in the [PropertyContainer]
 * @see MutableAssociatedProperties
 * @see PropertyContainer
 * @see Property
 * @see com.quarkdown.core.ast.attributes.AstAttributes.properties for an example of usage
 */
interface AssociatedProperties<T : Any, V> {
    /**
     * Retrieves the [PropertyContainer] associated with the given key, also registering an empty new one to it if it doesn't exist.
     * @param key the key to retrieve the [PropertyContainer] for
     * @return the [PropertyContainer] associated with the key
     */
    fun of(key: T): PropertyContainer<V>
}

/**
 * Mutable implementation of [AssociatedProperties].
 */
class MutableAssociatedProperties<T : Any, V> : AssociatedProperties<T, V> {
    private val properties = ConcurrentCache<T, MutablePropertyContainer<V>>()

    override fun of(key: T): MutablePropertyContainer<V> = properties.getOrPut(key) { MutablePropertyContainer() }
}
