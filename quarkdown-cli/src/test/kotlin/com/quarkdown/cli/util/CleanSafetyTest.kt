package com.quarkdown.cli.util

import com.quarkdown.cli.TempDirectory
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tests for [checkCleanSafety].
 */
class CleanSafetyTest : TempDirectory() {
    private lateinit var sandboxHome: File
    private lateinit var sandboxCwd: File

    @BeforeTest
    fun setUp() {
        reset()
        sandboxHome = File(directory, "home").apply { mkdirs() }
        sandboxCwd = File(directory, "cwd").apply { mkdirs() }
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    private fun File.check(sourceFile: File? = null): CleanRefusal? =
        checkCleanSafety(
            sourceFile = sourceFile,
            homeDirectory = sandboxHome.canonicalFile,
            workingDirectory = sandboxCwd.canonicalFile,
        )

    @Test
    fun `safe directory has no refusal`() {
        val out = File(directory, "build/output").apply { mkdirs() }
        assertNull(out.check())
    }

    @Test
    fun `filesystem root is refused`() {
        assertEquals(CleanRefusal.FilesystemRoot, File("/").check())
    }

    @Test
    fun `home directory is refused`() {
        assertEquals(CleanRefusal.HomeDirectory, sandboxHome.check())
    }

    @Test
    fun `working directory is refused`() {
        assertEquals(CleanRefusal.WorkingDirectory, sandboxCwd.check())
    }

    @Test
    fun `directory containing a git folder is refused`() {
        val projectRoot = File(directory, "project").apply { mkdirs() }
        File(projectRoot, ".git").mkdirs()

        assertEquals(CleanRefusal.RepositoryRoot, projectRoot.check())
    }

    @Test
    fun `directory containing a git pointer file is refused`() {
        val projectRoot = File(directory, "submodule").apply { mkdirs() }
        File(projectRoot, ".git").writeText("gitdir: ../.git/modules/sub")

        assertEquals(CleanRefusal.RepositoryRoot, projectRoot.check())
    }

    @Test
    fun `directory containing the source file is refused`() {
        val out = File(directory, "out").apply { mkdirs() }
        val source = File(out, "main.qd").apply { writeText("hello") }

        val refusal = assertIs<CleanRefusal.ContainsSourceFile>(out.check(sourceFile = source))
        assertEquals(source.canonicalFile, refusal.source)
    }

    @Test
    fun `directory containing the source file in a nested directory is refused`() {
        val out = File(directory, "out").apply { mkdirs() }
        val nested = File(out, "deep/nested").apply { mkdirs() }
        val source = File(nested, "main.qd").apply { writeText("hello") }

        val refusal = assertIs<CleanRefusal.ContainsSourceFile>(out.check(sourceFile = source))
        assertEquals(source.canonicalFile, refusal.source)
    }

    @Test
    fun `directory not containing the source file is safe`() {
        val out = File(directory, "out").apply { mkdirs() }
        val source = File(directory, "main.qd").apply { writeText("hello") }

        assertNull(out.check(sourceFile = source))
    }

    @Test
    fun `sibling directory of source file is safe`() {
        val sibling = File(directory, "sibling").apply { mkdirs() }
        val source =
            File(directory, "other/main.qd").apply {
                parentFile.mkdirs()
                writeText("hello")
            }

        assertNull(sibling.check(sourceFile = source))
    }
}
