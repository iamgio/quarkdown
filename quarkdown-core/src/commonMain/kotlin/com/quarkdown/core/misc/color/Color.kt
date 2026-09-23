package com.quarkdown.core.misc.color

import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * A color with red, green, blue and alpha components.
 * @param red red component (0-255)
 * @param green green component (0-255)
 * @param blue blue component (0-255)
 * @param alpha alpha component (0.0-1.0)
 */
data class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Double = MAX_ALPHA,
) : RenderRepresentable {
    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)

    /**
     * @see com.quarkdown.core.misc.color.decoder.decode
     */
    companion object {
        /**
         * Maximum value for RGB components.
         */
        const val MAX_RGB = 255

        /**
         * Maximum value for alpha component.
         */
        const val MAX_ALPHA = 1.0
    }
}
