package com.quarkdown.core.lexer.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LineTest {
    private fun lines(source: String) = Lines.of(source)

    @Test
    fun `splits on newlines without a trailing empty line`() {
        assertEquals(listOf("a", "b"), lines("a\nb").map { it.text })
        assertEquals(listOf("a"), lines("a\n").map { it.text })
        assertEquals(listOf("a", ""), lines("a\n\n").map { it.text })
        assertEquals(emptyList(), lines("").map { it.text })
        assertEquals(listOf(""), lines("\n").map { it.text })
    }

    @Test
    fun `tracks boundaries and termination`() {
        val (first, second) = lines("ab\ncd")
        assertEquals(0, first.start)
        assertEquals(2, first.end)
        assertEquals(3, first.nextStart)
        assertTrue(first.isTerminated)
        assertEquals(3, second.start)
        assertEquals(5, second.end)
        assertEquals(5, second.nextStart)
        assertFalse(second.isTerminated)
    }

    @Test
    fun `expands tabs to four column stops`() {
        assertEquals(4, lines("\tx").single().indent)
        assertEquals(4, lines("  \t x").single().indent.let { it - 1 })
        assertEquals(2, lines("  x").single().indent)
        assertEquals(0, lines("x").single().indent)
    }

    @Test
    fun `reports blankness and content`() {
        assertTrue(lines("   ").single().isBlank)
        assertTrue(lines("\t").single().isBlank)
        assertFalse(lines("  x").single().isBlank)
        assertEquals("x  ", lines("  x  ").single().content)
        assertEquals(2, lines("  x  ").single().contentStart)
    }

    @Test
    fun `tells an empty line from a blank one and counts literal spaces`() {
        assertTrue(lines("a\n\n").last().isEmpty)
        assertFalse(lines("   ").single().isEmpty)
        assertEquals(0, lines("\tx").single().leadingSpaces)
        assertEquals(2, lines("  \tx").single().leadingSpaces)
        assertEquals("bc", lines("abcd").single().sourceSlice(1, 3))
    }

    @Test
    fun `counts runs of a character at its content start`() {
        assertEquals(3, lines("   ### h").single().countContentRun('#'))
        assertEquals(0, lines("   ### h").single().countContentRun('>'))
    }
}
