package com.quarkdown.core

import com.quarkdown.core.util.IOUtils
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [IOUtils] file resolution and path utilities.
 */
class IOUtilsTest {
    private val root = File.listRoots().first().absolutePath

    private fun abs(vararg segments: String) = File(root + segments.joinToString(File.separator))

    @Test
    fun `isSubPath returns true for child`() {
        assertTrue(IOUtils.isSubPath(parent = abs("a", "b"), child = abs("a", "b", "c", "d")))
    }

    @Test
    fun `isSubPath returns false for sibling`() {
        assertFalse(IOUtils.isSubPath(parent = abs("a", "b"), child = abs("a", "c")))
    }

    @Test
    fun `isSubPath normalizes paths`() {
        assertTrue(IOUtils.isSubPath(parent = abs("a", "b"), child = File(abs("a", "b", "..", "b", "c").path)))
    }

    @Test
    fun `resolvePath resolves relative path from working directory`() {
        val result = IOUtils.resolvePath("sub/file.txt", workingDirectory = abs("project"))
        assertEquals(File(abs("project"), "sub/file.txt"), result)
    }

    @Test
    fun `resolvePath resolves absolute path ignoring working directory`() {
        val absolutePath = abs("absolute", "file.txt").path
        val result = IOUtils.resolvePath(absolutePath, workingDirectory = abs("project"))
        assertEquals(File(absolutePath), result)
    }

    @Test
    fun `resolvePath resolves relative path as-is when working directory is null`() {
        val result = IOUtils.resolvePath("relative/file.txt", workingDirectory = null)
        assertEquals(File("relative/file.txt"), result)
    }
}
