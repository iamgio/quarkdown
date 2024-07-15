package eu.iamgio.quarkdown.document.page

import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor

/**
 * A numeric size with a unit, which represents a generic size (e.g. margin, length, font size).
 */
data class Size(val value: Double, val unit: Unit) : RenderRepresentable {
    override fun toString() = "$value${unit.name.lowercase()}"

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)

    /**
     * Unit of a [Size].
     */
    enum class Unit {
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
}

/**
 * Represents a size expressed in pixels.
 */
val Double.px: Size
    get() = Size(this, Size.Unit.PX)

/**
 * Represents a size expressed in centimeters.
 */
val Double.cm: Size
    get() = Size(this, Size.Unit.CM)

/**
 * Represents a size expressed in millimeters.
 */
val Double.mm: Size
    get() = Size(this, Size.Unit.MM)

/**
 * Represents a size expressed in inches.
 */
val Double.inch: Size
    get() = Size(this, Size.Unit.IN)
