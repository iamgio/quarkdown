package eu.iamgio.quarkdown

/**
 * System properties wrapper, used to define properties that affect execution.
 */
object SystemProperties {
    /**
     * When this property is present, the rendering stage produces pretty output code.
     */
    const val PRETTY_OUTPUT = "pretty"

    /**
     * @return the corresponding property value for [key], if it exists
     */
    operator fun get(key: String): String? = System.getProperty(key)

    /**
     * Sets the system property of key [key] to value [value].
     */
    operator fun set(
        key: String,
        value: String,
    ) {
        System.setProperty(key, value)
    }

    /**
     * @return whether [key] is a defined system property
     */
    fun contains(key: String): Boolean = this[key] != null
}

// Helpers

/**
 * Whether the rendering stage should produce pretty output code
 */
val SystemProperties.isPrettyOutputEnabled: Boolean
    get() = contains(PRETTY_OUTPUT)
