package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.tokens.ReferenceFootnoteToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReferenceRecognizersTest {
    private fun reference(source: String) = ReferenceLinkRecognizer.recognize("$source\n", 0)?.token as ReferenceLinkToken?

    private fun footnote(source: String) = ReferenceFootnoteRecognizer.recognize("$source\n", 0)?.token as ReferenceFootnoteToken?

    @Test
    fun `reads the three reference link shapes`() {
        assertEquals("r", reference("[l][r]")!!.reference)
        assertNull(reference("[l][]")!!.reference)
        assertNull(reference("[l]")!!.reference)
        assertEquals("[l][r]", reference("[l][r]")!!.data.text)
    }

    @Test
    fun `reads footnote references and all-in-one definitions`() {
        assertEquals("f", footnote("[^f]")!!.label)
        assertNull(footnote("[^f]")!!.definition)
        assertEquals("d", footnote("[^f: d]")!!.definition)
        assertEquals("d", footnote("[^: d]")!!.definition)
        assertNull(footnote("[f]"))
    }
}
