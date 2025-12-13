package com.quarkdown.test

import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.test.util.execute
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for persistent headings via [com.quarkdown.core.ast.quarkdown.inline.LastHeading].
 */
class PersistentHeadingTest {
    @BeforeTest
    fun setUp() {
        PageMarginContentInitializer.resetCounter()
    }

    @Test
    fun `unavailable in plain documents`() {
        assertFailsWith<InvalidFunctionCallException> {
            execute(".lastheading depth:{2}") { }
        }
    }

    @Test
    fun `in margin content`() {
        execute(
            """
            .doctype {paged}
            
            .pagemargin {topcenter}
                .lastheading depth:{2}
            
            ## Heading
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-margin-content page-margin-top-center\" " +
                    "data-margin-id=\"page-margin-1\" data-margin-position=\"top-center\" " +
                    "data-on-left-page=\"top-center\" data-on-right-page=\"top-center\">" +
                    "<span class=\"last-heading\" data-depth=\"2\"></span>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-1\" aria-hidden=\"true\" hidden=\"\"></span>" +
                    "<h2>Heading</h2>",
                it,
            )
        }
    }

    @Test
    fun `with emphasis, in margin content`() {
        execute(
            """
             .doctype {slides}
            
            .pagemargin {topcenter}
                *.lastheading depth:{2}*
            
            ## Heading
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-margin-content page-margin-top-center\" " +
                    "data-margin-id=\"page-margin-1\" data-margin-position=\"top-center\" " +
                    "data-on-left-page=\"top-center\" data-on-right-page=\"top-center\">" +
                    "<p><em><span class=\"last-heading\" data-depth=\"2\"></span></em></p>" +
                    "</div>" +
                    "<span class=\"page-margin-switch\" data-margin-id=\"page-margin-1\" aria-hidden=\"true\" hidden=\"\"></span>" +
                    "<h2>Heading</h2>",
                it,
            )
        }
    }
}
