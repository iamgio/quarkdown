package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for math nodes and TeX integration.
 */
class MathTest {
    @Test
    fun `inline math`() {
        execute("Hello $ \\frac {x} {2} $") {
            assertEquals(
                "<p>Hello <formula>\\frac {x} {2}</formula></p>",
                it,
            )
        }
    }

    @Test
    fun `multiple inline math in the same paragraph`() {
        execute("$ \\frac {x} {2} $ and $ \\sqrt {x + 1} $") {
            assertEquals(
                "<p><formula>\\frac {x} {2}</formula> and <formula>\\sqrt {x + 1}</formula></p>",
                it,
            )
        }
    }

    @Test
    fun `one-line block math`() {
        execute("$ \\frac {x} {2} $") {
            assertEquals(
                "<formula data-block=\"\">\\frac {x} {2}</formula>",
                it,
            )
            assertEquals(0, documentInfo.tex.macros.size)
        }
    }

    @Test
    fun `one-line block math with inner dollar sign`() {
        execute("$ \\frac {x} {2}$ $") {
            assertEquals(
                "<formula data-block=\"\">\\frac {x} {2}$</formula>",
                it,
            )
            assertEquals(0, documentInfo.tex.macros.size)
        }
    }

    @Test
    fun `multiline block math`() {
        execute(
            """
            $$$
            f(x) = \begin{cases}
                1 & \text{if } x > 0 \\
                0 & \text{otherwise}
            \end{cases}
            $$$
            """.trimIndent(),
        ) {
            assertEquals(
                """
                <formula data-block="">f(x) = \begin{cases}
                    1 & \text{if } x > 0 \\
                    0 & \text{otherwise}
                \end{cases}</formula>
                """.trimIndent(),
                it,
            )
        }
    }

    @Test
    fun `custom macro`() {
        execute(
            """
            .texmacro {\hello}
                Hello \textit {world}
            $ \hello $
            """.trimIndent(),
        ) {
            assertEquals(1, documentInfo.tex.macros.size)
            val macro = documentInfo.tex.macros["\\hello"]
            assertEquals("Hello \\textit {world}", macro)
        }
    }
}
