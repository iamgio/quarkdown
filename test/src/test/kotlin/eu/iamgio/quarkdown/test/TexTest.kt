package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for TeX integration.
 */
class TexTest {
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
