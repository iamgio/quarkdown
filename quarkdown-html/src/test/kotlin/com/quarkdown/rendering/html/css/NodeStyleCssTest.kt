package com.quarkdown.rendering.html.css

import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.misc.color.Color
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the [CssBuilder] extension functions that inject a [NodeStyle] or a [TextTransformData]
 * as CSS declarations, used when rendering stylable nodes such as headings and containers.
 */
class NodeStyleCssTest {
    @Test
    fun `empty NodeStyle produces no declarations`() {
        assertEquals("", css { all(NodeStyle()) })
    }

    @Test
    fun `foreground color is emitted as color`() {
        assertEquals(
            "color: rgba(255, 0, 0, 1.0);",
            css { all(NodeStyle(foregroundColor = Color(255, 0, 0))) },
        )
    }

    @Test
    fun `background color is emitted as background-color`() {
        assertEquals(
            "background-color: rgba(0, 255, 0, 1.0);",
            css { all(NodeStyle(backgroundColor = Color(0, 255, 0))) },
        )
    }

    @Test
    fun `margin, padding and corner radius are emitted with four sides`() {
        val sizes = Sizes(Size(1.0, Size.Unit.CENTIMETERS))
        assertEquals(
            "margin: 1.0cm 1.0cm 1.0cm 1.0cm; " +
                "padding: 1.0cm 1.0cm 1.0cm 1.0cm; " +
                "border-radius: 1.0cm 1.0cm 1.0cm 1.0cm;",
            css { all(NodeStyle(margin = sizes, padding = sizes, cornerRadius = sizes)) },
        )
    }

    @Test
    fun `alignment and text alignment are emitted`() {
        assertEquals(
            "justify-items: center; text-align: end;",
            css {
                all(
                    NodeStyle(
                        alignment = NodeStyle.Alignment.CENTER,
                        textAlignment = NodeStyle.TextAlignment.END,
                    ),
                )
            },
        )
    }

    @Test
    fun `border color alone implies a solid border style`() {
        assertEquals(
            "border-color: rgba(10, 20, 30, 1.0); border-style: solid;",
            css { all(NodeStyle(borderColor = Color(10, 20, 30))) },
        )
    }

    @Test
    fun `border width alone implies a solid border style`() {
        assertEquals(
            "border-width: 2.0px 2.0px 2.0px 2.0px; border-style: solid;",
            css { all(NodeStyle(borderWidth = Sizes(Size(2.0, Size.Unit.PIXELS)))) },
        )
    }

    @Test
    fun `explicit border style is preserved over the implicit solid fallback`() {
        assertEquals(
            "border-color: rgba(10, 20, 30, 1.0); border-style: dashed;",
            css {
                all(
                    NodeStyle(
                        borderColor = Color(10, 20, 30),
                        borderStyle = NodeStyle.BorderStyle.DASHED,
                    ),
                )
            },
        )
    }

    @Test
    fun `text transform fields carried by a NodeStyle are appended after other declarations`() {
        assertEquals(
            "background-color: rgba(0, 255, 0, 1.0); font-weight: bold; text-decoration: line-through;",
            css {
                all(
                    NodeStyle(
                        backgroundColor = Color(0, 255, 0),
                        textTransform =
                            TextTransformData(
                                weight = TextTransformData.Weight.BOLD,
                                decoration = TextTransformData.Decoration.STRIKETHROUGH,
                            ),
                    ),
                )
            },
        )
    }

    @Test
    fun `textTransform emits every non-null field in declared order`() {
        assertEquals(
            "font-size: var(--qd-size-large, 1em); " +
                "font-weight: bold; " +
                "font-style: italic; " +
                "font-variant: small-caps; " +
                "text-decoration: underline; " +
                "text-transform: uppercase; " +
                "color: rgba(255, 0, 0, 1.0);",
            css {
                textTransform(
                    TextTransformData(
                        size = TextTransformData.Size.LARGE,
                        weight = TextTransformData.Weight.BOLD,
                        style = TextTransformData.Style.ITALIC,
                        variant = TextTransformData.Variant.SMALL_CAPS,
                        decoration = TextTransformData.Decoration.UNDERLINE,
                        case = TextTransformData.Case.UPPERCASE,
                        color = Color(255, 0, 0),
                    ),
                )
            },
        )
    }

    @Test
    fun `empty textTransform produces no declarations`() {
        assertEquals("", css { textTransform(TextTransformData()) })
    }
}
