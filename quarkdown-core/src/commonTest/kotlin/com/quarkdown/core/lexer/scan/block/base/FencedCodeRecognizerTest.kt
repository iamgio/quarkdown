package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.FencesCodeToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FencedCodeRecognizerTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    private fun fence(source: String) = FencedCodeRecognizer.open(LineCursor(Lines.of(source), 0))?.token as FencesCodeToken?

    @Test
    fun `reads the header parts right to left`() {
        val full = fence("```lang \"cap\" {#i}\nc\n```\n")!!
        assertEquals("lang", full.language)
        assertEquals("\"cap\"", full.caption)
        assertEquals("i", full.customId)
        assertEquals("\nc\n", full.content)
        assertNull(fence("``` \"cap\"\nc\n```\n")!!.language)
        assertEquals("\"cap\"", fence("``` \"cap\"\nc\n```\n")!!.caption)
        assertNull(fence("```\nc\n```\n")!!.language)
    }

    @Test
    fun `reads a lone custom id as a custom id rather than as the language`() {
        val idOnly = fence("``` {#i}\nc\n```\n")!!
        assertNull(idOnly.language)
        assertEquals("i", idOnly.customId)
    }

    @Test
    fun `closes on a longer run of the same character and ignores the other one`() {
        assertEquals("```\nc\n````", fence("```\nc\n````\n")?.data?.text)
        assertEquals("~~~\n```\n~~~", fence("~~~\n```\n~~~\n")?.data?.text)
        assertNull(fence("``\nc\n``\n"))
    }

    @Test
    fun `runs to the end of the input when its fence is never closed`() {
        assertEquals("```\nc\n~~~", fence("```\nc\n~~~\n")?.data?.text)
        assertEquals("\nc\n~~~", fence("```\nc\n~~~\n")?.content)
        assertEquals("```\nc", fence("```\nc\n")?.data?.text)
    }

    @Test
    fun `takes a fence run in the middle of a line as content`() {
        assertEquals("```\na```\n```", fence("```\na```\n```\n")?.data?.text)
        assertEquals("\na```\n", fence("```\na```\n```\n")?.content)
        assertEquals("```\ncode ```\nstill code\n```", fence("```\ncode ```\nstill code\n```\n")?.data?.text)
    }

    @Test
    fun `needs the closing fence to start its line, indented by at most three columns`() {
        assertEquals("\nc\n   ", fence("```\nc\n   ```\n")?.content)
        assertEquals("\nc\n     ```", fence("```\nc\n     ```\n")?.content)
    }

    @Test
    fun `records the opening indentation`() {
        assertEquals(2, fence("  ```\n  c\n  ```\n")!!.indent)
    }

    @Test
    fun `closes only on a fence at least as long as the opening one`() {
        assertEquals("````\nc\n```\nd\n````", fence("````\nc\n```\nd\n````\n")?.data?.text)
        assertEquals("\nc\n```\nd\n", fence("````\nc\n```\nd\n````\n")?.content)
        assertEquals("````\nc\n```", fence("````\nc\n```\n")?.data?.text)
    }

    @Test
    fun `tells a fence opener from a lookalike`() {
        assertTrue(FencedCodeRecognizer.opensAt(line("```lang")))
        assertTrue(FencedCodeRecognizer.opensAt(line("~~~~")))
        assertFalse(FencedCodeRecognizer.opensAt(line("``")))
    }
}
