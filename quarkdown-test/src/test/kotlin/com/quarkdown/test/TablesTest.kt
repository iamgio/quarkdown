package com.quarkdown.test

import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Markdown tables.
 */
class TablesTest {
    @Test
    fun `one-row table`() {
        execute("| Header 1 | Header 2 |\n|----------|----------|\n| Cell 1   | Cell 2   |") {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody></table>",
                it,
            )
        }
    }

    @Test
    fun `one-row table with math`() {
        execute("| Header 1 | Header 2 |\n|----------|----------|\n| $ X $ | $ Y $ |") {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td><formula>X</formula></td>" +
                    "<td><formula>Y</formula></td></tr></tbody></table>",
                it,
            )
            assertTrue(attributes.hasMath) // Ensures the tree traversal visits table cells too.
        }
    }

    @Test
    fun `one-row table with alignment`() {
        execute("| Header 1 | Header 2 | Header 3 |\n|:---------|:--------:|---------:|\n| Cell 1   | Cell 2   | Cell 3   |") {
            assertEquals(
                "<table><thead><tr><th align=\"left\">Header 1</th><th align=\"center\">Header 2</th>" +
                    "<th align=\"right\">Header 3</th></tr></thead><tbody><tr><td align=\"left\">Cell 1</td>" +
                    "<td align=\"center\">Cell 2</td><td align=\"right\">Cell 3</td></tr></tbody></table>",
                it,
            )
        }
    }

    @Test
    fun `one-row table with alignment and caption`() {
        execute(
            """
            | Header 1 | Header 2 | Header 3 |
            |----------|:--------:|----------|
            | Cell 1   | Cell 2   | Cell 3   |
            'Table caption'
            """.trimIndent(),
        ) {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th align=\"center\">Header 2</th>" +
                    "<th>Header 3</th></tr></thead><tbody><tr><td>Cell 1</td>" +
                    "<td align=\"center\">Cell 2</td><td>Cell 3</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\">Table caption</caption></table>",
                it,
            )
        }
    }

    @Test
    fun `multi-row table`() {
        execute(
            """
            | Header 1 | Header 2 |
            |----------|----------|
            | Cell 1   | Cell 2   |
            | Cell 3   | Cell 4   |
            """.trimIndent(),
        ) {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr>" +
                    "<tr><td>Cell 3</td><td>Cell 4</td></tr></tbody></table>",
                it,
            )
        }
    }

    // #40
    @Test
    fun `unformatted table`() {
        execute(
            """
            | Topic                                                                        | Description                                                                                                                               |
            |------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
            | [link 1](./practical-lion-recognition)                    | Describes the directory structure and exactly how tigers and lions are often mistaken for jaguars by application developers |
            | [link2/stuff/and more](./picnic-control-1.xy)             | Provides a worked example that shows how picnics can be controlled at a den, pack and organization level                                |
            | [link3/production and stuff](./example-stuff.xy)                    | Provides a worked example that shows                                                        |
            | [link4](internal-Service-Structure.xy)                            | Describes the internal components of the  and how they work                                                    |
            | [Link5 is established](identity.xy)                                   | Describes how user and workloads identities                                                               | 
            | Link the sixth                                                                 | Description of the externally accessible REST and gRPC API                                                                                |
            | And the seventh                                                        |  the source code for the different components are laid out                                |
            """.trimIndent(),
        ) {
            assertTrue(it.startsWith("<table>"))
            assertContains(it, "link4")
            assertTrue(it.endsWith("</table>"))
        }
    }
}
