package com.quarkdown.core.filesystem

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Behavioral contract for [FileSystem] implementations.
 */
sealed class FileSystemContractTest {
    /** A file system whose working directory points at the seeded tree root. */
    protected abstract val fs: FileSystem

    @Test
    fun `resolves relative paths against the working directory`() {
        assertEquals("content", fs.resolve("file.txt").readText())
        assertEquals("content", fs.resolve("sub/../file.txt").normalized.readText())
    }

    @Test
    fun `resolves absolute paths as-is`() {
        val absolute = fs.resolve("file.txt")
        assertTrue(absolute.isAbsolute)
        assertEquals(absolute, fs.resolve(absolute.fullPath))
    }

    @Test
    fun `is root until branched`() {
        assertTrue(fs.isRoot)
        assertNull(fs.root)
        val branched = fs.branch(fs.resolve("sub"))
        assertFalse(branched.isRoot)
        assertEquals(fs, branched.root)
        // Branching from a branch keeps the original root.
        val branchedTwice = branched.branch(fs.workingDirectory)
        assertEquals(fs, branchedTwice.root)
    }

    @Test
    fun `branched file system resolves from its own working directory`() {
        val branched = fs.branch(fs.resolve("sub"))
        assertTrue(branched.resolve("inner.txt").exists)
    }

    @Test
    fun `reroot creates an independent root on the same backend`() {
        val rerooted = fs.reroot(fs.resolve("sub"))
        assertTrue(rerooted.isRoot)
        assertTrue(rerooted.resolve("inner.txt").exists)
    }

    @Test
    fun `computes relative paths between file systems`() {
        val branched = fs.branch(fs.resolve("sub"))
        assertEquals("sub", fs.relativePathTo(branched)?.invariantSeparatorsPath)
        assertEquals("..", branched.relativePathTo(fs)?.invariantSeparatorsPath)
        assertEquals(".", fs.relativePathTo(fs)?.invariantSeparatorsPath)
    }

    @Test
    fun `relative path is null without a working directory`() {
        assertNull(fs.reroot(null).relativePathTo(fs))
        assertNull(fs.relativePathTo(fs.reroot(null)))
    }
}

class DiskFileSystemTest : FileSystemContractTest() {
    private val tempDir: File by lazy { Files.createTempDirectory("filesystem-test").toFile() }

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    override val fs: FileSystem by lazy {
        File(tempDir, "sub").mkdirs()
        File(tempDir, "file.txt").writeText("content")
        File(tempDir, "sub/inner.txt").writeText("inner")
        DiskFileSystem(tempDir)
    }

    @Test
    fun `entries are disk-backed`() {
        assertEquals(
            File(tempDir, "file.txt").canonicalFile,
            fs.resolve("file.txt").canonical.toFileOrNull(),
        )
    }
}

class VirtualFileSystemTest : FileSystemContractTest() {
    override val fs: VirtualFileSystem by lazy {
        VirtualFileSystem("/project").apply {
            mkdirs("sub")
            write("file.txt", "content")
            write("sub/inner.txt", "inner")
        }
    }

    @Test
    fun `entries are not disk-backed`() {
        assertNull(fs.resolve("file.txt").toFileOrNull())
    }

    @Test
    fun `write creates missing parent directories`() {
        fs.write("deep/tree/leaf.txt", "leaf")
        assertEquals("leaf", fs.resolve("deep/tree/leaf.txt").readText())
    }
}
