package com.quarkdown.core.property

/**
 * A group of properties, associated with their own key.
 * @see MutablePropertyContainer
 * @see Property
 * @see AssociatedProperties
 */
interface PropertyContainer {
    operator fun <T> get(key: Property.Key<T>): T?
}

/**
 * Mutable implementation of [PropertyContainer].
 */
class MutablePropertyContainer : PropertyContainer {
    private val properties: MutableMap<Property.Key<*>, Property<*>> = mutableMapOf()

    /**
     * Adds a property to the container.
     * @param property the property to add
     */
    fun addProperty(property: Property<*>) {
        properties[property.key] = property
    }

    /**
     * @see addProperty
     */
    operator fun plusAssign(property: Property<*>) = addProperty(property)

    /**
     * Retrieves a property from the container by its key.
     * @param key the key of the property to retrieve
     * @return the property associated with the key, if any
     */
    @Suppress("UNCHECKED_CAST") // Safe to assume the property has the same generic type as the key.
    override operator fun <T> get(key: Property.Key<T>): T? = properties[key]?.value as? T
}
