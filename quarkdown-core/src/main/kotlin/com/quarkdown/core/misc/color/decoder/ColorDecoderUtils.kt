package com.quarkdown.core.misc.color.decoder

import com.quarkdown.core.misc.color.Color

/**
 * @param awtColor Java AWT color
 * @return a [Color] from a Java AWT color
 */
fun Color.Companion.from(awtColor: java.awt.Color): Color =
    Color(
        red = awtColor.red,
        green = awtColor.green,
        blue = awtColor.blue,
        alpha = awtColor.alpha.toDouble() / 255,
    )

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
