package com.quarkdown.cli

import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.exec.ReplCommand
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the `repl` command, fed with scripted standard input.
 */
class ReplCommandTest : TempDirectory() {
    private val outputDirectory = File(directory, "out")

    /**
     * Runs the REPL over [input] and returns what it printed to standard output.
     */
    private fun runRepl(
        input: String,
        vararg args: String,
    ): String {
        val stdout = ByteArrayOutputStream()
        val originalOut = System.out
        val originalIn = System.`in`
        try {
            System.setOut(PrintStream(stdout))
            System.setIn(ByteArrayInputStream(input.toByteArray()))
            ReplCommand().test(listOf("--out", outputDirectory.absolutePath, *args))
        } finally {
            System.setOut(originalOut)
            System.setIn(originalIn)
        }
        return stdout.toString()
    }

    @Test
    fun `prints the result of each input`() {
        val output = runRepl(".var {x} {5}\n.x\nexit\n", "--nowrap")
        assertTrue("<p>5</p>" in output)
        assertFalse("<!DOCTYPE html>" in output)
    }

    @Test
    fun `wraps the result unless disabled`() {
        val output = runRepl("Hello\nexit\n")
        assertTrue("<!DOCTYPE html>" in output)
        assertTrue("<p>Hello</p>" in output)
    }

    @Test
    fun `writes no file`() {
        runRepl("Hello\nexit\n")
        assertFalse(outputDirectory.exists())
    }
}
