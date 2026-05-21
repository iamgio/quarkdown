package com.quarkdown.cli.doctor

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.QuarkdownCommand
import com.quarkdown.installlayout.InstallLayout
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the `doctor get install-dir` command.
 */
class DoctorGetInstallDirCommandTest {
    private fun runCommand(): String =
        QuarkdownCommand()
            .subcommands(DoctorCommand())
            .test("doctor get install-dir")
            .output
            .trim()

    @Test
    fun `prints the resolved install directory`() {
        val expected = InstallLayout.get.file.parentFile.absolutePath
        assertEquals(expected, runCommand())
    }

    @Test
    fun `printed path is absolute and points to an existing directory`() {
        val printed = File(runCommand())
        assertTrue(printed.isAbsolute)
        assertTrue(printed.isDirectory)
    }

    @Test
    fun `printed directory contains either lib or dev-lib`() {
        // In a real installation the parent contains `lib/` (alongside `bin/` and `docs/`);
        // in dev test runs it contains `dev-lib/` (the layout mirror produced by `assembleDevLib`).
        val printed = File(runCommand())
        val children = printed.list()?.toSet().orEmpty()
        assertTrue(
            "lib" in children || "dev-lib" in children,
            "Expected install directory $printed to contain either 'lib' or 'dev-lib', got: $children",
        )
    }
}
