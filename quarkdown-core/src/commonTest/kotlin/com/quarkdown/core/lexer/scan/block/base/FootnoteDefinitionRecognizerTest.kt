package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.scan.block.BaseInterruptions
import com.quarkdown.core.lexer.tokens.FootnoteDefinitionToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FootnoteDefinitionRecognizerTest {
    private val recognizer = FootnoteDefinitionRecognizer(BaseInterruptions.paragraph)

    private fun footnote(source: String) = recognizer.open(LineCursor(Lines.of(source), 0))?.token as FootnoteDefinitionToken?

    @Test
    fun `reads the label and a one-line definition`() {
        val one = footnote("[^f]: one\n")!!
        assertEquals("f", one.label)
        assertEquals("one", one.content)
        assertEquals("[^f]: one", one.data.text)
    }

    @Test
    fun `continues like a paragraph`() {
        assertEquals("[^f]: multi\nline", footnote("[^f]: multi\nline\n\npara\n")?.data?.text)
        assertEquals("[^f]: a", footnote("[^f]: a\n# h\n")?.data?.text)
    }

    @Test
    fun `declines non-footnotes`() {
        assertNull(footnote("[f]: x\n"))
        assertNull(footnote("[^]: x\n"))
        assertNull(footnote("    [^f]: x\n"))
    }
}
