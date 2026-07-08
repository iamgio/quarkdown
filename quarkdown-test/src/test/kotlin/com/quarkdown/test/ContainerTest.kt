package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.container` ([com.quarkdown.core.ast.quarkdown.block.Container]).
 */
class ContainerTest {
    @Test
    fun `empty container`() {
        execute(".container") {
            assertEquals("<div class=\"container\"></div>", it)
        }
    }

    @Test
    fun `body only`() {
        execute(
            """
            .container
                Hello, **world**!
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\"><p>Hello, <strong>world</strong>!</p></div>",
                it,
            )
        }
    }

    @Test
    fun `width and height`() {
        execute(
            """
            .container width:{4cm} height:{2cm}
                Sized
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" style=\"width: 4.0cm; height: 2.0cm;\"><p>Sized</p></div>",
                it,
            )
        }
    }

    @Test
    fun fullwidth() {
        execute(
            """
            .container fullwidth:{yes}
                Fills the row
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container fullwidth\"><p>Fills the row</p></div>",
                it,
            )
        }
    }

    @Test
    fun `foreground and background colors`() {
        execute(
            """
            .container foreground:{#111} background:{#eee}
                Colors
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" " +
                    "style=\"color: rgba(17, 17, 17, 1.0); background-color: rgba(238, 238, 238, 1.0);\">" +
                    "<p>Colors</p></div>",
                it,
            )
        }
    }

    @Test
    fun `border with color and width`() {
        execute(
            """
            .container border:{#333} borderwidth:{2px}
                Bordered
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" " +
                    "style=\"border-color: rgba(51, 51, 51, 1.0); border-width: 2.0px 2.0px 2.0px 2.0px; border-style: solid;\">" +
                    "<p>Bordered</p></div>",
                it,
            )
        }
    }

    @Test
    fun `explicit border style`() {
        execute(
            """
            .container border:{#333} borderwidth:{2px} borderstyle:{dashed}
                Bordered
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" " +
                    "style=\"border-color: rgba(51, 51, 51, 1.0); border-width: 2.0px 2.0px 2.0px 2.0px; border-style: dashed;\">" +
                    "<p>Bordered</p></div>",
                it,
            )
        }
    }

    @Test
    fun `padding and margin and radius`() {
        execute(
            """
            .container padding:{1cm} margin:{2cm} radius:{4px}
                Spaced
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" " +
                    "style=\"margin: 2.0cm 2.0cm 2.0cm 2.0cm; padding: 1.0cm 1.0cm 1.0cm 1.0cm; " +
                    "border-radius: 4.0px 4.0px 4.0px 4.0px;\">" +
                    "<p>Spaced</p></div>",
                it,
            )
        }
    }

    @Test
    fun `alignment (default textalignment)`() {
        execute(
            """
            .container alignment:{center}
                Centered
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" style=\"justify-items: center; text-align: center;\"><p>Centered</p></div>",
                it,
            )
        }
    }

    @Test
    fun `alignment with independent textalignment`() {
        execute(
            """
            .container alignment:{start} textalignment:{end}
                Independent
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" style=\"justify-items: start; text-align: end;\"><p>Independent</p></div>",
                it,
            )
        }
    }

    @Test
    fun classname() {
        execute(
            """
            .container classname:{fancy}
                Custom
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container fancy\"><p>Custom</p></div>",
                it,
            )
        }
    }

    @Test
    fun `nested containers`() {
        execute(
            """
            .container padding:{1cm}
                .container background:{#eee}
                    Inner
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"container\" style=\"padding: 1.0cm 1.0cm 1.0cm 1.0cm;\">" +
                    "<div class=\"container\" style=\"background-color: rgba(238, 238, 238, 1.0);\">" +
                    "<p>Inner</p>" +
                    "</div></div>",
                it,
            )
        }
    }
}
