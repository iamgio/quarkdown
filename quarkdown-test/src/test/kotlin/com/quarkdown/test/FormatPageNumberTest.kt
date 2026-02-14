package com.quarkdown.test

import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatPageNumberTest {
    @Test
    fun `format page number`() {
        execute(
            """
            .doctype {paged}
            .doclang {english}
            .formatpagenumber format:{i}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"page-number-formatter\" data-format=\"i\" data-hidden=\"\"></div>",
                it,
            )
        }
    }
}
