package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val MERMAID_OPEN = "<figure><pre class=\"mermaid fill-height\">"
private const val MERMAID_CLOSE = "</pre></figure>"

/**
 * Tests for Mermaid diagrams.
 */
class MermaidTest {
    @Test
    fun mermaid() {
        execute(
            """
            .mermaid
                graph TD
                    A-->B
                    A-->C
            """.trimIndent(),
        ) {
            assertEquals("${MERMAID_OPEN}graph TD\n    A-->B\n    A-->C$MERMAID_CLOSE", it)
            assertTrue(attributes.hasMermaidDiagram)
        }
    }

    @Test
    fun `mermaid with caption`() {
        execute(
            """
            .mermaid caption:{My graph}
                graph TD
                    A-->B
                    A-->C
            """.trimIndent(),
        ) {
            assertEquals(
                "${MERMAID_OPEN}graph TD\n    A-->B\n    A-->C</pre>" +
                        "<figcaption>My graph</figcaption></figure>",
                it,
            )
            assertTrue(attributes.hasMermaidDiagram)
        }
    }

    @Test
    fun `mermaid from file`() {
        execute(
            """
            .mermaid caption:{My graph}
                .read {mermaid/class.mmd}
            """.trimIndent(),
        ) {
            assertEquals(it.lines().first(), MERMAID_OPEN + "classDiagram")
            assertEquals(it.lines()[1], "    class Bank {")
            assertContains(it, "<figcaption>My graph</figcaption></figure>")
        }
    }

    @Test
    fun `simple xy chart`() {
        execute(
            """
              .xychart
                - 5000
                - 6000
                - 7500
            """.trimIndent()
        ) {
            assertEquals(
                MERMAID_OPEN +
                        "xychart-beta\n\t" +
                        "line [5000.0, 6000.0, 7500.0]\n" +
                        MERMAID_CLOSE,
                it,
            )
            assertTrue(attributes.hasMermaidDiagram)
        }
    }

    @Test
    fun `simple xy chart with bars`() {
        execute(
            """
              .xychart bars:{yes}
                - 5000
                - 6000
                - 7500
            """.trimIndent()
        ) {
            assertEquals(
                MERMAID_OPEN +
                        "xychart-beta\n" +
                        "\tline [5000.0, 6000.0, 7500.0]\n" +
                        "\tbar [5000.0, 6000.0, 7500.0]\n" +
                        MERMAID_CLOSE,
                it,
            )
            assertTrue(attributes.hasMermaidDiagram)
        }
    }
}
