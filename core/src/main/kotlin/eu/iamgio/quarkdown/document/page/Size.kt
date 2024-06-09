package eu.iamgio.quarkdown.document.page

import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor

/**
 * A numeric size with a unit, which represents a generic size (e.g. margin, length, font size).
 */
data class Size(val value: Double, val unit: SizeUnit) : RenderRepresentable {
    override fun toString() = "$value${unit.name.lowercase()}"

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
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
