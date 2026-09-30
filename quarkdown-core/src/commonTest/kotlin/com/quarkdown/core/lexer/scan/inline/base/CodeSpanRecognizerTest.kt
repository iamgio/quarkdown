package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.tokens.CodeSpanToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CodeSpanRecognizerTest {
    private fun span(source: String) = CodeSpanRecognizer.recognize("$source\n", 0)?.token as CodeSpanToken?

    @Test
    fun `matches runs of equal length`() {
        assertEquals("x", span("`x`")!!.content)
        assertEquals("x", span("``x``")!!.content)
        assertEquals(" x ", span("` x `")!!.content)
        assertEquals("a`b", span("``a`b``")!!.content)
    }

    @Test
    fun `declines unbalanced and empty spans`() {
        assertNull(span("`x"))
        assertNull(span("``"))
        assertNull(span("`x``"))
    }
}
