package com.quarkdown.cli.doctor

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.QuarkdownCommand
import kotlin.test.Test
import kotlin.test.assertContains

/**
 * Tests for the `doctor env` command.
 */
class DoctorEnvCommandTest {
    private fun runCommand(): String =
        QuarkdownCommand()
            .subcommands(DoctorCommand())
            .test("doctor env")
            .output

    @Test
    fun `reports JVM and Chrome rows`() {
        val output = runCommand()
        assertContains(output, "JVM")
        assertContains(output, "Chrome (PDF export)")
    }

    @Test
    fun `JVM is reported as found with the current runtime version`() {
        val output = runCommand()
        assertContains(output, "found")
        assertContains(output, Runtime.version().toString())
    }
}
