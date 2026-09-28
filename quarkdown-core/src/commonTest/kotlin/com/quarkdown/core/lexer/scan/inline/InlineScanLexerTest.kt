package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.scan.inline.base.CriticalContentRecognizer
import com.quarkdown.core.lexer.tokens.PlainTextToken
import kotlin.test.Test
import kotlin.test.assertEquals

class InlineScanLexerTest {
    private fun dump(source: String) =
        InlineScanLexer(source, listOf(CriticalContentRecognizer), ::PlainTextToken)
            .tokenize()
            .joinToString(" ") {
                val kind = if (it is PlainTextToken) "P" else "C"
                "$kind[${it.data.text.replace("\n", "\\n")}]@${it.data.position}"
            }

    @Test
    fun `accumulates plain text between matches`() {
        assertEquals("P[a]@0..0 C[&]@1..1 P[b]@2..2", dump("a&b"))
        assertEquals("P[abc]@0..2", dump("abc"))
        assertEquals("C[<]@0..0 C[>]@1..1", dump("<>"))
    }

    @Test
    fun `covers every index exactly once and terminates`() {
        listOf("a", "&", "a&&b", "a".repeat(500) + "&")
            .forEach { source ->
                val tokens = InlineScanLexer(source, listOf(CriticalContentRecognizer), ::PlainTextToken).tokenize().toList()
                assertEquals(source.length, tokens.sumOf { it.data.text.length }, "coverage for <$source>")
                assertEquals(source, tokens.joinToString("") { it.data.text }, "order for <$source>")
            }
    }

    @Test
    fun `emits nothing for an empty source and drops no fill when asked not to`() {
        assertEquals("", dump(""))
        assertEquals(0, InlineScanLexer("abc", listOf(CriticalContentRecognizer), fill = null).tokenize().count())
    }
}
