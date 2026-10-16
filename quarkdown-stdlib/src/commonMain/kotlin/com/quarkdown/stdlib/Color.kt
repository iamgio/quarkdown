@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.function.reflect.annotation.LikelyChained
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.misc.color.Color
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Builds a color, either from scratch or from an existing one.
 * Each given component replaces the corresponding one of [base], while the others are kept.
 * Without a base, unspecified components start from opaque black.
 *
 * ```
 * .color red:{66} green:{144} blue:{245}
 * ```
 * Builds `rgb(66, 144, 245)`.
 *
 * ```
 * .text {Faded} color:{.color {#4290F5} alpha:{0.5}}
 * ```
 * Displays the text at half opacity.
 *
 * ```
 * .var {brand} {#4290F5}
 * .container background:{.brand::color alpha:{0.25}}
 * ```
 * Derives a translucent variant of a stored color.
 *
 * @param base the color to start from. Opaque black if unset
 * @param red red component, 0-255
 * @param green green component, 0-255
 * @param blue blue component, 0-255
 * @param alpha alpha component, 0-1
 * @return the resulting color
 * @throws IllegalArgumentException if a component is out of range
 * @wiki color
 */
@QFunction
@LikelyChained
fun color(
    base: Color = Color(0, 0, 0),
    @LikelyNamed red: Int = base.red,
    @LikelyNamed green: Int = base.green,
    @LikelyNamed blue: Int = base.blue,
    @LikelyNamed alpha: Number = base.alpha,
): ObjectValue<Color> {
    fun requireInRange(
        name: String,
        value: Number,
        max: Number,
    ) = require(value.toDouble() in 0.0..max.toDouble()) { "$name must be in the 0-$max range, but was $value" }

    requireInRange("Red", red, Color.MAX_RGB)
    requireInRange("Green", green, Color.MAX_RGB)
    requireInRange("Blue", blue, Color.MAX_RGB)
    requireInRange("Alpha", alpha, Color.MAX_ALPHA)

    return ObjectValue(Color(red, green, blue, alpha.toDouble()))
}
