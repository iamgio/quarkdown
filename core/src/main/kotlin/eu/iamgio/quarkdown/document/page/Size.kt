package eu.iamgio.quarkdown.document.page

import eu.iamgio.quarkdown.rendering.html.CssRepresentable

/**
 * A numeric size with a unit, which represents a generic size (e.g. margin, length, font size).
 */
data class Size(val value: Double, val unit: SizeUnit) : CssRepresentable {
    override fun toString() = "$value${unit.name.lowercase()}"

    override val asCSS: String
        get() = toString()
}

/**
 * Unit of a [Size].
 */
enum class SizeUnit {
    /**
     * Pixels.
     */
    PX,

    /**
     * Points.
     */
    PT,

    /**
     * Centimeters.
     */
    CM,

    /**
     * Millimeters.
     */
    MM,

    /**
     * Inches.
     */
    IN,
}
