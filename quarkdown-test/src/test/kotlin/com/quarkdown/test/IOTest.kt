package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for IO functions.
 */
class IOTest {
    @Test
    fun `read file`() {
        execute(".read {code.txt}") {
            assertEquals(
                "<p>Line 1\nLine 2\n\nLine 3</p>",
                it,
            )
        }
    }

    @Test
    fun `list files`() {
        execute(".listfiles {include} sortby:{name} order:{descending} fullpath:{no}") {
            assertEquals(
                "<ol>" +
                    (7 downTo 1).joinToString(separator = "") { n -> "<li><p>include-$n.md</p></li>" } +
                    "</ol>",
                it,
            )
        }
    }
}
