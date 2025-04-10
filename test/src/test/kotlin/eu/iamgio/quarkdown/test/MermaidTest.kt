package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            assertEquals("<figure><pre class=\"mermaid fill-height\">graph TD\n    A-->B\n    A-->C</pre></figure>", it)
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
                "<figure><pre class=\"mermaid fill-height\">graph TD\n    A-->B\n    A-->C</pre>" +
                    "<figcaption>My graph</figcaption></figure>",
                it,
            )
            assertTrue(attributes.hasMermaidDiagram)
        }
    }
}
