package com.quarkdown.stdlib.external

import com.quarkdown.core.filesystem.VirtualFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QdLibrariesTest {
    private val fileSystem =
        VirtualFileSystem().apply {
            write("/lib/qd/paper.qd", ".function {greet}\n  Hello")
            write("/lib/qd/docs.qd", "")
            write("/lib/qd/README.md", "not a library")
            write("/lib/qd/nested/hidden.qd", "")
            write("/lib/file.txt", "")
        }

    private fun load(path: String) = QdLibraries.fromDirectory(fileSystem.resolve(path))

    @Test
    fun `loads one library per qd file, named after the file`() {
        val names = load("/lib/qd").map { it.library.name }.toSet()
        assertEquals(setOf("paper", "docs"), names)
    }

    @Test
    fun `ignores other extensions and nested directories`() {
        assertEquals(2, load("/lib/qd").size)
    }

    @Test
    fun `empty directory yields no libraries`() {
        fileSystem.mkdirs("/empty")
        assertEquals(emptySet(), load("/empty"))
    }

    @Test
    fun `missing directory is rejected`() {
        assertFailsWith<IllegalArgumentException> { load("/lib/missing") }
    }

    @Test
    fun `file is rejected`() {
        assertFailsWith<IllegalArgumentException> { load("/lib/file.txt") }
    }
}
