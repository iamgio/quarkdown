package eu.iamgio.quarkdown.misc.color.decoder

import eu.iamgio.quarkdown.misc.color.Color

/**
 * Decodes a [Color] from a hexadecimal string (e.g. `#FF0000`).
 */
object HexColorDecoder : ColorDecoder {
    override fun decode(raw: String): Color? {
        if (raw.firstOrNull() != '#') return null
        val hex = raw.drop(1) // The '#' character is skipped.

        return try {
            Color(
                red = hex.substring(0, 2).toInt(16),
                green = hex.substring(2, 4).toInt(16),
                blue = hex.substring(4, 6).toInt(16),
            )
        } catch (e: NumberFormatException) {
            null
        }
    }
}
