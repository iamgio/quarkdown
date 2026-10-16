package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the color functions.
 */
class ColorTest {
    @Test
    fun `color derived from a base as a styling argument`() {
        execute(".heading {Hello} depth:{2} foreground:{.color {red} green:{100} alpha:{0.5}}") {
            assertEquals(
                "<h2 style=\"color: rgba(255, 100, 0, 0.5);\">Hello</h2>",
                it,
            )
        }
    }

    @Test
    fun `color built from components`() {
        execute(".heading {Hello} depth:{2} foreground:{.color red:{66} green:{144} blue:{245}}") {
            assertEquals(
                "<h2 style=\"color: rgba(66, 144, 245, 1.0);\">Hello</h2>",
                it,
            )
        }
    }

    @Test
    fun `color stored in a variable and chained`() {
        execute(
            """
            .var {brand} {#4290F5}
            .heading {Hello} depth:{2} background:{.brand::color alpha:{0.25}}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2 style=\"background-color: rgba(66, 144, 245, 0.25);\">Hello</h2>",
                it,
            )
        }
    }
}
