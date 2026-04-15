package com.quarkdown.core

import com.quarkdown.core.util.IOUtils
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
    fun `isSubPath resolves symlinks`() {
        val tempDir = Files.createTempDirectory("isSubPathTest")
        try {
            val projectDir = tempDir.resolve("project").createDirectories()
            val outsideFile = Files.createFile(tempDir.resolve("secret.txt"))
            val symlink = Files.createSymbolicLink(projectDir.resolve("link.txt"), outsideFile)

            assertFalse(IOUtils.isSubPath(parent = projectDir.toFile(), child = symlink.toFile()))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
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

    // Checksum

    @Test
    fun `checksum is stable for the same file`() {
        val file = Files.createTempFile("checksum", ".txt").toFile()
        try {
            file.writeText("hello")
            assertEquals(IOUtils.computeChecksum(file), IOUtils.computeChecksum(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `checksum changes when file content changes`() {
        val file = Files.createTempFile("checksum", ".txt").toFile()
        try {
            file.writeText("hello")
            val first = IOUtils.computeChecksum(file)
            file.writeText("world")
            assertNotEquals(first, IOUtils.computeChecksum(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `checksum is stable for the same directory`() {
        val dir = Files.createTempDirectory("checksumDir").toFile()
        try {
            dir.resolve("a.txt").writeText("aaa")
            dir.resolve("sub").mkdir()
            dir.resolve("sub/b.txt").writeText("bbb")
            assertEquals(IOUtils.computeChecksum(dir), IOUtils.computeChecksum(dir))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `directory checksum changes when a file is added`() {
        val dir = Files.createTempDirectory("checksumDir").toFile()
        try {
            dir.resolve("a.txt").writeText("aaa")
            val first = IOUtils.computeChecksum(dir)
            dir.resolve("b.txt").writeText("bbb")
            assertNotEquals(first, IOUtils.computeChecksum(dir))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `directory checksum changes when a file size changes`() {
        val dir = Files.createTempDirectory("checksumDir").toFile()
        try {
            dir.resolve("a.txt").writeText("short")
            val first = IOUtils.computeChecksum(dir)
            dir.resolve("a.txt").writeText("much longer content")
            assertNotEquals(first, IOUtils.computeChecksum(dir))
        } finally {
            dir.deleteRecursively()
        }
    }
}
