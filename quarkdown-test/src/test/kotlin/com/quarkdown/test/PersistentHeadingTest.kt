package com.quarkdown.test

import com.quarkdown.core.function.error.InvalidFunctionCallException
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for persistent headings via [com.quarkdown.core.ast.quarkdown.inline.LastHeading].
 */
class PersistentHeadingTest {
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
                "<div class=\"page-margin-content page-margin-top-center\">" +
                    "<span class=\"last-heading\" data-depth=\"2\"></span>" +
                    "</div>" +
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
                "<div class=\"page-margin-content page-margin-top-center\">" +
                    "<p><em><span class=\"last-heading\" data-depth=\"2\"></span></em></p>" +
                    "</div>" +
                    "<h2>Heading</h2>",
                it,
            )
        }
    }
}
