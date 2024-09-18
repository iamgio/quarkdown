package eu.iamgio.quarkdown.misc.color.decoder

import eu.iamgio.quarkdown.function.reflect.ReflectionUtils
import eu.iamgio.quarkdown.misc.color.Color

/**
 * Decodes a [Color] from the name (case-insensitive) of a native [java.awt.Color] color (e.g. `red`)
 * @see java.awt.Color
 */
object NamedColorDecoder : ColorDecoder {
    override fun decode(raw: String): Color? {
        // Name representation (e.g. red, GREEN, bLuE).
        val awtColor = ReflectionUtils.getConstantByName<java.awt.Color>(raw)
        return awtColor?.let { Color.from(it) }
    }
}
