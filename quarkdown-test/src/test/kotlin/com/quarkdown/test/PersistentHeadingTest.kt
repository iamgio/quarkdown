package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for persistent headings via [com.quarkdown.core.ast.quarkdown.inline.LastHeading].
 */
class PersistentHeadingTest {
    @Test
    fun `in margin content`() {
        execute(
            """
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
}
