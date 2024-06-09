package eu.iamgio.quarkdown.rendering.html

/**
 * Represents an object that can be converted to a CSS value.
 */
interface CssRepresentable {
    /**
     * Representation of this object as the value of a CSS entry.
     */
    val asCSS: String
}

/**
 * Returns this enum entry's name as a CSS property.
 */
val Enum<*>.asCSS: String
    get() = name.lowercase().replace("_", "-")
