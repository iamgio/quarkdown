package com.quarkdown.test

import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        val files = File(DATA_FOLDER, "include").listFiles()!!
        execute(".listfiles {include} sortby:{name} order:{descending} fullpath:{no}") {
            assertTrue(it.startsWith("<ol>"))
            files.forEach { file ->
                assertContains(it, "<li><p>${file.name}</p></li>")
            }
            assertTrue(it.endsWith("</ol>"))
        }
    }
}
