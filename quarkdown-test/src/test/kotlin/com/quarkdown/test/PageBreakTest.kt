package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for page breaks, both via the `<<<` syntax and the `.pagebreak` primitive function.
 */
class PageBreakTest {
    @Test
    fun `syntax page break`() {
        execute(
            """
            Hello

            <<<

            World
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello</p>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<p>World</p>",
                it,
            )
        }
    }

    @Test
    fun `page break primitive`() {
        execute(
            """
            Hello

            .pagebreak

            World
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello</p>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<p>World</p>",
                it,
            )
        }
    }
}
