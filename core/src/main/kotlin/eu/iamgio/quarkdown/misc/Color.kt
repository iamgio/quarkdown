package eu.iamgio.quarkdown.misc

import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor

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
    val alpha: Double = 1.0,
) : RenderRepresentable {
    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)

    companion object {
        /**
         * @param hex hexadecimal color code
         * @return a [Color] from a hex color code
         */
        fun fromHex(hex: String): Color {
            val hexValue = hex.removePrefix("#")
            return Color(
                red = hexValue.substring(0, 2).toInt(16),
                green = hexValue.substring(2, 4).toInt(16),
                blue = hexValue.substring(4, 6).toInt(16),
            )
        }

        /**
         * @param awtColor Java AWT color
         * @return a [Color] from a Java AWT color
         */
        fun fromAWT(awtColor: java.awt.Color): Color =
            Color(
                red = awtColor.red,
                green = awtColor.green,
                blue = awtColor.blue,
                alpha = awtColor.alpha.toDouble() / 255,
            )
    }
}
