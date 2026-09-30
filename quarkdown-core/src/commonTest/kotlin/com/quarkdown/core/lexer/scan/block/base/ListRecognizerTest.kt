package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.scan.block.BaseInterruptions
import com.quarkdown.core.lexer.tokens.OrderedListToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ListRecognizerTest {
    private val unordered = ListRecognizer(BaseInterruptions.list, ListKind.UNORDERED)
    private val ordered = ListRecognizer(BaseInterruptions.list, ListKind.ORDERED)

    private fun text(
        recognizer: ListRecognizer,
        source: String,
    ) = recognizer
        .open(LineCursor(Lines.of("$source\n"), 0))
        ?.token
        ?.data
        ?.text

    @Test
    fun `reaches the end of the source including the padded newline`() {
        assertEquals("- a\n- b\n", text(unordered, "- a\n- b"))
        assertEquals("- a\nlazy\n", text(unordered, "- a\nlazy"))
        assertEquals("10. x\npara\n", text(ordered, "10. x\npara"))
    }

    @Test
    fun `ends before a bullet of another kind`() {
        assertEquals("- a", text(unordered, "- a\n* b"))
        assertEquals("1. a\n10. b\n", text(ordered, "1. a\n10. b"))
        assertEquals("- a", text(unordered, "- a\n10. b"))
    }

    @Test
    fun `continues across a blank line that indented content or a same bullet resumes`() {
        assertEquals("- a\n  \n- b\n", text(unordered, "- a\n  \n- b"))
        assertEquals("- a\n \n- b\n", text(unordered, "- a\n \n- b"))
        assertEquals("- a\n\tx\n", text(unordered, "- a\n\tx"))
    }

    @Test
    fun `reads the ordered marker and declines the wrong kind`() {
        val token = ordered.open(LineCursor(Lines.of("3. a\n"), 0))!!.token as OrderedListToken
        assertEquals("3.", token.marker.trim())
        assertNull(text(ordered, "- a"))
        assertNull(text(unordered, "1. a"))
        assertNull(text(unordered, "    - a"))
    }
}
