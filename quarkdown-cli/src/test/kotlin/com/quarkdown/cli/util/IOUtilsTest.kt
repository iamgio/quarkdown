package com.quarkdown.cli.util

import com.quarkdown.cli.TempDirectory
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [cleanDirectory] and other CLI-level IO utilities.
 */
class IOUtilsTest : TempDirectory() {
    @BeforeTest
    fun setUp() {
        reset()
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `clean removes children but keeps directory`() {
        File(directory, "a.txt").writeText("a")
        File(directory, "b.txt").writeText("b")
        File(directory, "nested").apply {
            mkdir()
            File(this, "c.txt").writeText("c")
        }

        directory.cleanDirectory()

        assertTrue(directory.exists())
        assertEquals(emptyList(), directory.listFiles()?.toList())
    }

    @Test
    fun `clean on empty directory is a no-op`() {
        directory.cleanDirectory()

        assertTrue(directory.exists())
        assertEquals(emptyList(), directory.listFiles()?.toList())
    }

    @Test
    fun `clean on non-existent file is a no-op`() {
        val missing = File(directory, "missing")
        missing.cleanDirectory()

        assertFalse(missing.exists())
    }

    @Test
    fun `clean does not follow symlinks to external files`() {
        val externalTarget =
            Files.createTempDirectory("external-target").toFile().apply {
                File(this, "keepme.txt").writeText("keep me")
            }

        try {
            File(directory, "link")
                .toPath()
                .createSymbolicLinkPointingTo(externalTarget.toPath())

            directory.cleanDirectory()

            assertEquals(emptyList(), directory.listFiles()?.toList(), "Symlink entry should be removed")
            assertTrue(externalTarget.exists(), "External target directory should not be touched")
            assertTrue(
                File(externalTarget, "keepme.txt").exists(),
                "Files inside the symlink target should not be touched",
            )
        } finally {
            externalTarget.deleteRecursively()
        }
    }
}
