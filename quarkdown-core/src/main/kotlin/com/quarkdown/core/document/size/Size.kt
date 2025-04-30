package com.quarkdown.core.document.size

import com.quarkdown.core.rendering.html.CssRepresentableVisitor
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * A numeric size with a unit, which represents a generic size (e.g. margin, length, font size).
 */
data class Size(
    val value: Double,
    val unit: Unit,
) : RenderRepresentable {
    override fun toString() = this.accept(CssRepresentableVisitor()) // e.g. 10px, 5cm, 2in

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)

    /**
     * Unit of a [Size].
     */
    enum class Unit(
        val symbol: String,
    ) {
        PIXELS("px"),
        POINTS("pt"),
        CENTIMETERS("cm"),
        MILLIMETERS("mm"),
        INCHES("in"),
        PERCENTAGE("%"),
    }
}

/**
 * Represents a size expressed in pixels.
 */
val Double.px: Size
    get() = Size(this, Size.Unit.PIXELS)

/**
 * Represents a size expressed in pixels.
 */
val Int.px: Size
    get() = this.toDouble().px

/**
 * Represents a size expressed in centimeters.
 */
val Double.cm: Size
    get() = Size(this, Size.Unit.CENTIMETERS)

/**
 * Represents a size expressed in millimeters.
 */
val Double.mm: Size
    get() = Size(this, Size.Unit.MILLIMETERS)

/**
 * Represents a size expressed in inches.
 */
val Double.inch: Size
    get() = Size(this, Size.Unit.INCHES)

/**
 * Represents a size in percentage.
 */
val Int.percent: Size
    get() = Size(this.toDouble(), Size.Unit.PERCENTAGE)
