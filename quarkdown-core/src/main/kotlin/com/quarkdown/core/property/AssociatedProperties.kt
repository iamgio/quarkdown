package com.quarkdown.core.property

/**
 * Associations between a key of type [T] and a [PropertyContainer].
 * @param T the type of key elements
 * @see PropertyContainer
 * @see Property
 * @see com.quarkdown.core.ast.attributes.AstAttributes.properties for an example of usage
 */
class AssociatedProperties<T> {
    private val properties: MutableMap<T, PropertyContainer> = mutableMapOf()

    /**
     * Retrieves the [PropertyContainer] associated with the given key, also registering an empty new one to it if it doesn't exist.
     * @param key the key to retrieve the [PropertyContainer] for
     * @return the [PropertyContainer] associated with the key
     */
    fun of(key: T): PropertyContainer = properties.getOrPut(key) { PropertyContainer() }
}
