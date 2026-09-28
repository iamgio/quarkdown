package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThematicBreakRecognizerTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    private fun text(source: String) =
        ThematicBreakRecognizer
            .open(LineCursor(Lines.of(source), 0))
            ?.token
            ?.data
            ?.text

    @Test
    fun `covers the break line and the empty lines after it`() {
        assertEquals("---\n", text("---\n"))
        assertEquals("***\n\n", text("***\n\nP\n"))
        assertEquals("- - -\n", text("- - -\n"))
    }

    @Test
    fun `stops at a space-only line and declines non-breaks`() {
        assertEquals("---\n", text("---\n   \nP\n"))
        assertNull(text("--\n"))
        assertNull(text("-*-\n"))
    }

    @Test
    fun `tells a break line from a lookalike`() {
        listOf("---", "***", "___", "- - -", "-- -", "   ---   ", "-\t-\t-")
            .forEach { assertTrue(ThematicBreakRecognizer.opensAt(line(it)), it) }
        listOf("--", "-*-", "    ---", "--- a", "", "-a-", "-\u00a0-\u00a0-")
            .forEach { assertFalse(ThematicBreakRecognizer.opensAt(line(it)), it) }
    }
}
