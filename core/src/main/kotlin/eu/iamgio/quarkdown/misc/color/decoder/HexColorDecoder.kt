package eu.iamgio.quarkdown.misc.color.decoder

import com.github.ajalt.colormath.model.RGB
import eu.iamgio.quarkdown.misc.color.Color

/**
 * Decodes a [Color] from a hexadecimal string (e.g. `#FF0000`).
 */
object HexColorDecoder : ColorDecoder {
    override fun decode(raw: String): Color? {
        if (raw.firstOrNull() != '#') return null
        // Converted by Colormath.
        return try {
            Color.from(RGB(raw))
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
