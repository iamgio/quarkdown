package com.quarkdown.core.misc.color.decoder

import com.quarkdown.core.misc.color.Color

/**
 * Given a regex match result, extracts RGB components from it.
 * @return a list of red, green and blue components. Any can be `null` if the component is invalid.
 */
private fun extractRGBComponents(match: MatchResult): List<Int?> =
    match.destructured.toList().map { component ->
        component.toIntOrNull()?.takeIf { it <= Color.MAX_RGB }
    }

/**
 * Decodes a [Color] from an RGB string (e.g. `rgb(255, 100, 25)`).
 */
object RgbColorDecoder : ColorDecoder {
    override fun decode(raw: String): Color? {
        if (!raw.startsWith("rgb(")) return null

        val match = Regex("rgb\\((\\d{1,3}), ?(\\d{1,3}), ?(\\d{1,3})\\)").find(raw) ?: return null
        val (r, g, b) = extractRGBComponents(match)

        return if (r != null && g != null && b != null) {
            Color(r, g, b)
        } else {
            null
        }
    }
}

/**
 * Decodes a [Color] from an RGBA string (e.g. `rgba(255, 100, 25, 0.5)`).
 */
object RgbaColorDecoder : ColorDecoder {
    override fun decode(raw: String): Color? {
        if (!raw.startsWith("rgba(")) return null

        val match = Regex("rgba\\((\\d{1,3}), ?(\\d{1,3}), ?(\\d{1,3}), ?([0-9.]+)\\)").find(raw) ?: return null
        val (r, g, b) = extractRGBComponents(match)

        val a = match.destructured.component4().toDoubleOrNull()?.takeIf { it <= Color.MAX_ALPHA }

        return if (r != null && g != null && b != null && a != null) {
            Color(r, g, b, a)
        } else {
            null
        }
    }
}
