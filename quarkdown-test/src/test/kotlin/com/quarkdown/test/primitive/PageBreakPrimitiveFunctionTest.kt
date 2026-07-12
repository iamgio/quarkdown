package com.quarkdown.test.primitive

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `.extend` applied to page breaks.
 */
class PageBreakPrimitiveFunctionTest {
    @Test
    fun `no extension renders unchanged`() {
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
    fun `extension precedes every syntax page break with a sibling block`() {
        execute(
            """
            .extend {pagebreak}
                .container

                .super

            Hello

            <<<

            World
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello</p>" +
                    "<div class=\"container\"></div>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<p>World</p>",
                it,
            )
        }
    }

    @Test
    fun `extension precedes every primitive page break with a sibling block`() {
        execute(
            """
            .extend {pagebreak}
                .container

                .super

            Hello

            .pagebreak

            World
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello</p>" +
                    "<div class=\"container\"></div>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<p>World</p>",
                it,
            )
        }
    }
}
