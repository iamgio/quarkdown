package com.quarkdown.core.misc.color.decoder

import com.quarkdown.core.misc.color.Color

/**
 * @param colormathColor Colormath color
 * @return a [Color] from a Colormath color
 */
fun Color.Companion.from(colormathColor: com.github.ajalt.colormath.Color): Color =
    with(colormathColor.toSRGB()) {
        Color(
            red = (r * MAX_RGB).toInt(),
            green = (g * MAX_RGB).toInt(),
            blue = (b * MAX_RGB).toInt(),
        )
    }
