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
    @Test
    fun `isSubPath returns true for child`() {
        assertTrue(IOUtils.isSubPath(parent = File("/a/b"), child = File("/a/b/c/d")))
    }

    @Test
    fun `isSubPath returns false for sibling`() {
        assertFalse(IOUtils.isSubPath(parent = File("/a/b"), child = File("/a/c")))
    }

    @Test
    fun `isSubPath normalizes paths`() {
        assertTrue(IOUtils.isSubPath(parent = File("/a/b"), child = File("/a/b/../b/c")))
    }

    @Test
    fun `resolvePath resolves relative path from working directory`() {
        val result = IOUtils.resolvePath("sub/file.txt", workingDirectory = File("/project"))
        assertEquals(File("/project", "sub/file.txt"), result)
    }

    @Test
    fun `resolvePath resolves absolute path ignoring working directory`() {
        val result = IOUtils.resolvePath("/absolute/file.txt", workingDirectory = File("/project"))
        assertEquals(File("/absolute/file.txt"), result)
    }

    @Test
    fun `resolvePath resolves relative path as-is when working directory is null`() {
        val result = IOUtils.resolvePath("relative/file.txt", workingDirectory = null)
        assertEquals(File("relative/file.txt"), result)
    }
}
