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
 * Tests for the `doctor get agent-skill` command.
 */
class DoctorGetAgentSkillCommandTest {
    private fun runCommand(): String =
        QuarkdownCommand()
            .subcommands(DoctorCommand())
            .test("doctor get agent-skill")
            .output
            .trim()

    @Test
    fun `prints the resolved agent skill directory`() {
        val expected = InstallLayout.get.agentSkill.file.absolutePath
        assertEquals(expected, runCommand())
    }

    @Test
    fun `printed directory exists and contains SKILL md`() {
        val printed = File(runCommand())
        assertTrue(printed.isDirectory, "Expected an existing directory, got: $printed")
        assertTrue(File(printed, "SKILL.md").isFile, "Expected $printed to contain SKILL.md")
    }
}
