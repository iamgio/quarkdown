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
