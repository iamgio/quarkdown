package com.quarkdown.cli.util

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [promptUnless].
 */
class PromptUnlessTest {
    private enum class Size { SMALL, LARGE }

    /**
     * @param fallback value that skips the prompt, or `null` to prompt
     */
    private class SizeCommand(
        private val fallback: Size?,
    ) : CliktCommand() {
        var chosen: Size? = null

        private val size: Size by option("--size")
            .enum<Size>()
            .promptUnless("Size", default = "small") { fallback }

        override fun run() {
            chosen = size
        }
    }

    @Test
    fun `explicit option wins over the fallback`() {
        val command = SizeCommand(fallback = Size.LARGE)
        command.test(listOf("--size", "small"))
        assertEquals(Size.SMALL, command.chosen)
    }

    @Test
    fun `fallback skips the prompt`() {
        val command = SizeCommand(fallback = Size.LARGE)
        val result = command.test(emptyList(), stdin = "small\n")
        assertEquals(Size.LARGE, command.chosen)
        assertFalse("Size" in result.stdout)
    }

    @Test
    fun `prompt reads the answer when there is no fallback`() {
        val command = SizeCommand(fallback = null)
        val result = command.test(emptyList(), stdin = "LARGE\n")
        assertEquals(Size.LARGE, command.chosen)
        assertTrue("Size" in result.stdout)
    }

    @Test
    fun `empty answer yields the default`() {
        val command = SizeCommand(fallback = null)
        command.test(emptyList(), stdin = "\n")
        assertEquals(Size.SMALL, command.chosen)
    }

    @Test
    fun `closed input yields the default`() {
        val command = SizeCommand(fallback = null)
        command.test(emptyList(), stdin = "")
        assertEquals(Size.SMALL, command.chosen)
    }

    @Test
    fun `invalid answer is asked again`() {
        val command = SizeCommand(fallback = null)
        val result = command.test(emptyList(), stdin = "huge\nlarge\n")
        assertEquals(Size.LARGE, command.chosen)
        assertTrue("huge" in result.stdout || "huge" in result.stderr)
    }
}
