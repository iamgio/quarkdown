package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.test.util.execute
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
                    "<figcaption class=\"caption-bottom\">My graph</figcaption></figure>",
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
            assertContains(it, "<figcaption class=\"caption-bottom\">My graph</figcaption></figure>")
        }
    }

    private fun String.expectedChart() = MERMAID_OPEN + trimIndent().replace("    ", "\t") + "\n" + MERMAID_CLOSE

    @Test
    fun `xy chart`() {
        execute(
            """
            .xychart
              - 5000
              - 6000
              - 7500
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        line [5000.0, 6000.0, 7500.0]
                """.expectedChart(),
                it,
            )
            assertTrue(attributes.hasMermaidDiagram)
        }
    }

    @Test
    fun `xy chart with bars`() {
        execute(
            """
            .xychart bars:{yes}
              - 5000
              - 6000
              - 7500
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        bar [5000.0, 6000.0, 7500.0]
                        line [5000.0, 6000.0, 7500.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with named axis`() {
        execute(
            """
            .xychart x:{Months} y:{Revenue}
              - 5000
              - 6000
              - 7500
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        x-axis "Months"
                        y-axis "Revenue"
                        line [5000.0, 6000.0, 7500.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with ranged and named y axis`() {
        execute(
            """
            .xychart y:{Revenue} yrange:{2..8000}
              - 5000
              - 6000
              - 7500
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        y-axis "Revenue" 2 --> 8000
                        line [5000.0, 6000.0, 7500.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with open-ranged y axis`() {
        execute(
            """
            .xychart yrange:{..}
              - 5000
              - 6000
              - 7500
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        y-axis 5000.0 --> 7500.0
                        line [5000.0, 6000.0, 7500.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with function call in data`() {
        execute(
            """
            .var {x} {2}
            .xychart
              .repeat {3}
                .var {x} {.pow {.x} {2}}
                .x
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        line [4.0, 16.0, 256.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with three lines`() {
        execute(
            """
            .xychart
              - - 3
                - 2
                - 1
                
              - - 1
                - 2
                - 3
                
              - - 2
                - 1
                - 3
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        line [3.0, 2.0, 1.0]
                        line [1.0, 2.0, 3.0]
                        line [2.0, 1.0, 3.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with open-ranged x axis`() {
        execute(
            """
            .xychart xrange:{1..}
              - |
                - 3
                - 2
              - |
                - 1
                - 2
                - 3
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        x-axis 1 --> 3.0
                        line [3.0, 2.0]
                        line [1.0, 2.0, 3.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart with two curves`() {
        execute(
            """
            .xychart
              .repeat {3}
                .1::pow {2}
              
              .repeat {3}
                .1::sin::multiply {5}::round
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        line [4.0, 5.0, 1.0]
                        line [1.0, 4.0, 9.0]
                """.expectedChart(),
                it,
            )
        }
    }

    @Test
    fun `xy chart from csv`() {
        execute(
            """
            .xychart
                .tablecolumn {2}
                    .csv {csv/people.csv}
            """.trimIndent(),
        ) {
            assertEquals(
                """
                    xychart-beta
                        line [25.0, 32.0, 19.0]
                """.expectedChart(),
                it,
            )
        }
    }
}
