package com.quarkdown.core.filesystem

import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import okio.FileSystem as OkioBackend

/**
 * Behavioral contract for [FsEntry], run against both the disk and the in-memory backend.
 */
abstract class FsEntryContractTest {
    /** The backend under test. */
    protected abstract val backend: OkioBackend

    /** An absolute [FsEntry] pointing at the seeded root directory. */
    protected abstract val root: FsEntry

    /** Seeds the tree under [at]; called by subclasses while initializing [root]. */
    protected fun seed(at: FsEntry) {
        backend.createDirectories(at.path / "sub")
        backend.write(at.path / "file.txt") { writeUtf8("hello\nworld") }
        backend.write(at.path / "sub" / "nested.txt") { writeUtf8("nested") }
    }

    @Test
    fun `resolves child entries`() {
        val file = root.resolve("file.txt")
        assertEquals("file.txt", file.name)
        assertEquals("file", file.nameWithoutExtension)
        assertEquals(root, file.parent)
        assertTrue(file.isAbsolute)
    }

    @Test
    fun `queries existence and kind`() {
        assertTrue(root.resolve("file.txt").exists)
        assertTrue(root.resolve("file.txt").isFile)
        assertFalse(root.resolve("file.txt").isDirectory)
        assertTrue(root.resolve("sub").isDirectory)
        assertFalse(root.resolve("missing.txt").exists)
    }

    @Test
    fun `reads content`() {
        assertEquals("hello\nworld", root.resolve("file.txt").readText())
        assertEquals(listOf("hello", "world"), root.resolve("file.txt").readLines())
        assertEquals("nested", root.resolve("sub/nested.txt").readText())
        assertTrue(root.resolve("file.txt").readBytes().isNotEmpty())
    }

    @Test
    fun `lists children and descendants`() {
        assertEquals(
            setOf("file.txt", "sub"),
            root.children().map { it.name }.toSet(),
        )
        assertEquals(
            setOf("file.txt", "sub", "nested.txt"),
            root.descendants().map { it.name }.toSet(),
        )
    }

    @Test
    fun `computes relative paths`() {
        val nested = root.resolve("sub/nested.txt").normalized
        assertEquals("sub/nested.txt", nested.relativeTo(root)?.invariantSeparatorsPath)
    }

    @Test
    fun `detects sub-paths`() {
        assertTrue(root.resolve("sub/nested.txt").isSubPathOf(root))
        assertTrue(root.resolve("sub").isSubPathOf(root))
        assertFalse(root.parent!!.isSubPathOf(root))
        // Non-normalized escape.
        assertFalse(root.resolve("../outside.txt").isSubPathOf(root))
    }

    @Test
    fun `normalizes mixed separators`() {
        assertEquals("hello\nworld", root.resolve("./file.txt").normalized.readText())
    }
}

class DiskFsEntryTest : FsEntryContractTest() {
    private val tempDir: File by lazy { Files.createTempDirectory("fsentry-test").toFile() }

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    override val backend: OkioBackend = OkioBackend.SYSTEM
    override val root: FsEntry by lazy {
        val entry = FsEntry(tempDir.toOkioPath(), backend)
        seed(entry)
        entry.canonical
    }

    @Test
    fun `converts to java io File`() {
        assertEquals(tempDir.canonicalFile, root.toFileOrNull())
    }

    @Test
    fun `sub-path detection resolves symlinks`() {
        val outside = root.resolve("secret.txt")
        Files.createFile(outside.toFileOrNull()!!.toPath())
        val symlink = root.resolve("sub/link.txt")
        Files.createSymbolicLink(symlink.toFileOrNull()!!.toPath(), outside.toFileOrNull()!!.toPath())

        // The symlink lives inside sub/, but points outside of it.
        assertFalse(symlink.isSubPathOf(root.resolve("sub")))
    }
}

class VirtualFsEntryTest : FsEntryContractTest() {
    override val backend: OkioBackend = FakeFileSystem()
    override val root: FsEntry by lazy {
        val entry = FsEntry("/project".toPath(), backend)
        seed(entry)
        entry
    }

    @Test
    fun `does not convert to java io File`() {
        assertNull(root.toFileOrNull())
    }
}
