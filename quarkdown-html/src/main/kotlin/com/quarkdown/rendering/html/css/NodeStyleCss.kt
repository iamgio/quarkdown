package com.quarkdown.rendering.html.css

import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData

/**
 * Applies all properties of a [NodeStyle] to this [CssBuilder].
 * @param style the style to apply
 */
fun CssBuilder.all(style: NodeStyle) {
    "color" value style.foregroundColor
    "background-color" value style.backgroundColor
    "margin" value style.margin
    "padding" value style.padding
    "border-color" value style.borderColor
    "border-width" value style.borderWidth
    "border-radius" value style.cornerRadius

    "border-style" value
        when {
            // If the border style is set, it is used.
            style.borderStyle != null -> style.borderStyle

            // If border properties are set, a normal (solid) border is used.
            style.borderColor != null || style.borderWidth != null -> NodeStyle.BorderStyle.NORMAL

            // No border style.
            else -> null
        }

    "justify-items" value style.alignment
    "text-align" value style.textAlignment
    style.textTransform?.let(::textTransform)
}

/**
 * Applies all properties of a [TextTransformData] to this [CssBuilder].
 * @param data the text transform data to apply
 */
fun CssBuilder.textTransform(data: TextTransformData) {
    "font-size" value data.size
    "font-weight" value data.weight
    "font-style" value data.style
    "font-variant" value data.variant
    "text-decoration" value data.decoration
    "text-transform" value data.case
    "color" value data.color
}
