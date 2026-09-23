package com.quarkdown.core.filesystem

import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VirtualFileSystemMutationTest {
    private val fs =
        VirtualFileSystem("/project").apply {
            write("main.qd", "# Main")
            write("chapters/one.qd", "One")
            write("chapters/two.qd", "Two")
        }

    @Test
    fun `deletes a file`() {
        fs.delete("main.qd")
        assertFalse(fs.resolve("main.qd").exists)
        assertTrue(fs.resolve("chapters/one.qd").exists)
    }

    @Test
    fun `deletes a directory tree`() {
        fs.delete("chapters")
        assertFalse(fs.resolve("chapters").exists)
        assertFalse(fs.resolve("chapters/one.qd").exists)
        assertEquals("# Main", fs.resolve("main.qd").readText())
    }

    @Test
    fun `deleting a missing path is a no-op`() {
        fs.delete("missing.qd")
        assertTrue(fs.resolve("main.qd").exists)
    }

    @Test
    fun `deleted file can be written again`() {
        fs.delete("main.qd")
        fs.write("main.qd", "# Again")
        assertEquals("# Again", fs.resolve("main.qd").readText())
    }

    @Test
    fun `moves a file`() {
        fs.move("main.qd", "index.qd")
        assertFalse(fs.resolve("main.qd").exists)
        assertEquals("# Main", fs.resolve("index.qd").readText())
    }

    @Test
    fun `moves a directory tree`() {
        fs.move("chapters", "parts")
        assertFalse(fs.resolve("chapters").exists)
        assertEquals("One", fs.resolve("parts/one.qd").readText())
        assertEquals("Two", fs.resolve("parts/two.qd").readText())
    }

    @Test
    fun `moves into a directory that does not exist yet`() {
        fs.move("main.qd", "archive/2026/main.qd")
        assertEquals("# Main", fs.resolve("archive/2026/main.qd").readText())
    }

    @Test
    fun `moving a missing source fails`() {
        assertFailsWith<IOException> { fs.move("missing.qd", "elsewhere.qd") }
    }
}
