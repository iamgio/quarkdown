package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
class CaptionTest {
    @Test
    fun figure() {
        execute(
            """
            ![](https://example.com/image.png "Figure caption")
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><img src=\"https://example.com/image.png\" alt=\"\" title=\"Figure caption\" />" +
                    "<figcaption>Figure caption</figcaption></figure>",
                it,
            )
        }
    }

    @Test
    fun `figure via function`() {
        execute(
            """
            .figure caption:{Figure caption}
                Hello
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure><p>Hello</p><figcaption>Figure caption</figcaption></figure>",
                it,
            )
        }
    }

    @Test
    fun table() {
        execute(
            """
            | Header 1 | Header 2 | Header 3 |
            |----------|:--------:|----------|
            | Cell 1   | Cell 2   | Cell 3   |
            "Table caption"
            """.trimIndent(),
        ) {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th align=\"center\">Header 2</th>" +
                    "<th>Header 3</th></tr></thead><tbody><tr><td>Cell 1</td>" +
                    "<td align=\"center\">Cell 2</td><td>Cell 3</td></tr></tbody>" +
                    "<caption>Table caption</caption></table>",
                it,
            )
        }
    }
}
