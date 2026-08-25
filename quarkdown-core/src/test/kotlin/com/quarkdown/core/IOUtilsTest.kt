package com.quarkdown.core

import com.quarkdown.core.util.IOUtils
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for [IOUtils] file resolution and path utilities.
 */
class IOUtilsTest {
    private val root = File.listRoots().first().absolutePath

    private fun abs(vararg segments: String) = File(root + segments.joinToString(File.separator))

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
