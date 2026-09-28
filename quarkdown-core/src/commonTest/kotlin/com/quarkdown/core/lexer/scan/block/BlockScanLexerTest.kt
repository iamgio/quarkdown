package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.scan.block.base.BlockTextRecognizer
import com.quarkdown.core.lexer.scan.block.base.NewlineRecognizer
import com.quarkdown.core.lexer.tokens.NewlineToken
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockScanLexerTest {
    private fun dump(source: String) =
        BlockScanLexer(source, listOf(NewlineRecognizer, BlockTextRecognizer))
            .tokenize()
            .joinToString(" ") {
                val kind = if (it is NewlineToken) "N" else "T"
                "$kind[${it.data.text.replace("\n", "\\n")}]@${it.data.position}"
            }

    @Test
    fun `emits nothing for an empty source`() {
        assertEquals("", dump(""))
    }

    @Test
    fun `absorbs runs of blank lines into one newline token, padding included`() {
        assertEquals("N[\\n\\n]@0..1", dump("\n"))
        assertEquals("N[\\n\\n\\n]@0..2", dump("\n\n"))
        assertEquals("N[   \\n]@0..3", dump("   "))
    }

    @Test
    fun `emits one token per non-blank line and drops the separator`() {
        assertEquals("T[a]@0..0 T[b]@2..2", dump("a\nb"))
        assertEquals("T[a]@0..0 N[\\n]@2..2", dump("a\n"))
    }

    @Test
    fun `normalizes line separators before scanning`() {
        assertEquals("T[a]@0..0 T[b]@2..2", dump("a\r\nb"))
    }
}
