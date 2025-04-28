package eu.iamgio.quarkdown.misc.color.decoder

import com.github.ajalt.colormath.model.HSL
import com.github.ajalt.colormath.model.HSV
import eu.iamgio.quarkdown.misc.color.Color

/**
 * Maximum value for the hue component.
 */
private const val MAX_HUE = 360

/**
 * Maximum value for saturation and lightness/value components.
 */
private const val MAX_SVL = 100

/**
 * Decodes a [Color] from an HSV or HSL string (e.g. `hsv(208, 70, 66)` or `hsl(208, 54, 43)`).
 */
object HsvHslColorDecoder : ColorDecoder {
    override fun decode(raw: String): Color? {
        if (!raw.startsWith("hsl(") && !raw.startsWith("hsv(")) return null

        // HSL and HSV have the same structure (a degree (0-360) and two percentages (0-100)).
        for (method in arrayOf('l', 'v')) {
            val match = Regex("hs$method\\((\\d{1,3}), ?(\\d{1,3}), ?(\\d{1,3})\\)").find(raw) ?: continue
            val (h, s, lv) =
                match.destructured.let { (h, s, lv) ->
                    Triple(
                        // Hue (0-360).
                        // e.g. 520 % 360 = 160
                        h
                            .toFloatOrNull()
                            ?.rem(MAX_HUE),
                        // Normalized saturation (0-1).
                        // [0, 100] -> [0, 1]
                        s
                            .toFloatOrNull()
                            ?.takeIf { it <= MAX_SVL }
                            ?.div(MAX_SVL),
                        // Normalized lightness/value (0-1).
                        // [0, 100] -> [0, 1]
                        lv
                            .toFloatOrNull()
                            ?.takeIf { it <= MAX_SVL }
                            ?.div(MAX_SVL),
                    )
                }

            if (h == null || s == null || lv == null) continue

            // Colormath color to be converted.
            val color =
                when (method) {
                    'l' -> HSL(h, s, lv)
                    'v' -> HSV(h, s, lv)
                    else -> return null // Impossible
                }

            return Color.from(color)
        }

        return null
    }
}
