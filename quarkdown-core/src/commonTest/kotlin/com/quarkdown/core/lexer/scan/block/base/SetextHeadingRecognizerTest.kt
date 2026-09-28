package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.SetextHeadingToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SetextHeadingRecognizerTest {
    private fun setext(source: String) = SetextHeadingRecognizer.open(LineCursor(Lines.of(source), 0))?.token as SetextHeadingToken?

    @Test
    fun `keeps every line before the underline`() {
        val multi = setext("A\nB\n===\n")!!
        assertEquals("A\nB", multi.content)
        assertEquals("===", multi.underline)
        assertEquals("A\nB\n===\n", multi.data.text)
    }

    @Test
    fun `reads the custom id and absorbs the empty lines after the underline`() {
        assertEquals("i", setext("A {#i}\n===\n")!!.customId)
        assertEquals("A", setext("A {#i}\n===\n")!!.content)
        assertEquals("A\n===\n\n", setext("A\n===\n\nP\n")?.data?.text)
    }

    @Test
    fun `declines bullets and an immediate underline`() {
        assertNull(setext("- A\n===\n"))
        assertNull(setext("===\n===\n"))
        assertNull(setext("A\nB\n"))
    }
}
