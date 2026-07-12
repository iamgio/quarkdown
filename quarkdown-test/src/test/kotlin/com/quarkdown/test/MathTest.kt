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
    fun `math primitive in inline context defaults to inline`() {
        execute("Hello .math {2 + 2}") {
            assertEquals(
                "<p>Hello <formula>2 + 2</formula></p>",
                it,
            )
        }
    }

    @Test
    fun `math primitive on its own line defaults to block`() {
        execute(".math {2 + 2}") {
            assertEquals(
                "<formula data-block=\"\">2 + 2</formula>",
                it,
            )
        }
    }

    @Test
    fun `math primitive block override forces block in inline context`() {
        execute("Hello .math {2 + 2} block:{yes}") {
            assertEquals(
                "<p>Hello <formula data-block=\"\">2 + 2</formula></p>",
                it,
            )
        }
    }

    @Test
    fun `math primitive block override forces inline on its own line`() {
        execute(".math {2 + 2} block:{no}") {
            assertEquals(
                "<formula>2 + 2</formula>",
                it,
            )
        }
    }

    @Test
    fun `math primitive block with reference`() {
        execute(".math {2 + 2} ref:{eq1}") {
            assertEquals(
                "<formula data-block=\"\" id=\"eq1\">2 + 2</formula>",
                it,
            )
        }
    }

    @Test
    fun `math primitive inline with styling`() {
        execute("Hello .math {2 + 2} foreground:{red}") {
            assertEquals(
                "<p>Hello <formula style=\"color: rgba(255, 0, 0, 1.0);\">2 + 2</formula></p>",
                it,
            )
        }
    }

    @Test
    fun `math primitive block with styling`() {
        execute(".math {2 + 2} background:{blue}") {
            assertEquals(
                "<formula data-block=\"\" style=\"background-color: rgba(0, 0, 255, 1.0);\">2 + 2</formula>",
                it,
            )
        }
    }

    @Test
    fun `math primitive body evaluates nested function calls`() {
        execute(
            """
            .math
                2 + 2 = .sum {2} {2}
            """.trimIndent(),
        ) {
            assertEquals(
                "<formula data-block=\"\">2 + 2 = 4</formula>",
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
