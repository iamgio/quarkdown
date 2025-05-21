package com.quarkdown.core.property

/**
 * A group of properties, associated with their own key.
 * @param T type of the properties
 * @see MutablePropertyContainer
 * @see Property
 * @see AssociatedProperties
 */
interface PropertyContainer<T> {
    /**
     * Retrieves a property from the container by its key.
     * @param key the key of the property to retrieve
     * @param V the type of the property, subtype of [T]
     * @return the property associated with the key, if any
     */
    operator fun <V : T> get(key: Property.Key<V>): V?
}

/**
 * Mutable implementation of [PropertyContainer].
 */
class MutablePropertyContainer<T> : PropertyContainer<T> {
    private val properties: MutableMap<Property.Key<out T>, Property<out T>> = mutableMapOf()

    /**
     * Adds a property to the container.
     * @param property the property to add
     * @param V the type of the property, subtype of [T]
     */
    fun <V : T> addProperty(property: Property<V>) {
        properties[property.key] = property
    }

    /**
     * @see addProperty
     */
    operator fun <V : T> plusAssign(property: Property<V>) = addProperty(property)

    @Suppress("UNCHECKED_CAST") // Safe to assume the property has the same generic type as the key.
    override operator fun <V : T> get(key: Property.Key<V>): V? = properties[key]?.value as? V
}
