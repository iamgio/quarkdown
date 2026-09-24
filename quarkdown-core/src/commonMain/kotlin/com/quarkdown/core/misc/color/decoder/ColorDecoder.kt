package com.quarkdown.core.misc.color.decoder

import com.quarkdown.core.misc.color.Color

/**
 * A strategy to decode a [Color] from a raw string.
 */
interface ColorDecoder {
    /**
     * Decodes a [Color] from a raw string.
     * @param raw raw string
     * @return a [Color] from the raw string according this the specific strategy, or `null` if the conversion fails
     */
    fun decode(raw: String): Color?
}

/**
 * Decodes a [Color] from a raw string using the first decoder that successfully decodes it.
 * @param raw raw string
 * @param decoders ordered list of decoders. Defaults to all decoders.
 * @return a successfully decoded [Color], or `null` if no decoder can decode it
 */
fun Color.Companion.decode(
    raw: String,
    vararg decoders: ColorDecoder =
        arrayOf(
            HexColorDecoder,
            RgbColorDecoder,
            RgbaColorDecoder,
            HsvHslColorDecoder,
            NamedColorDecoder,
        ),
): Color? = decoders.firstNotNullOfOrNull { it.decode(raw) }
