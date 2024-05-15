package eu.iamgio.quarkdown.document.page

/**
 * A numeric size with a unit, which represents a generic size (e.g. margin, length, font size).
 */
data class Size(val value: Double, val unit: SizeUnit) {
    override fun toString() = "$value${unit.name.lowercase()}"
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
