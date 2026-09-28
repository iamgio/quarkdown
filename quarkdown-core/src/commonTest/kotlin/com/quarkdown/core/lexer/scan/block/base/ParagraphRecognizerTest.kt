package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.block.BaseInterruptions
import com.quarkdown.core.lexer.scan.block.BlockScanLexer
import com.quarkdown.core.lexer.tokens.ParagraphToken
import kotlin.test.Test
import kotlin.test.assertEquals

class ParagraphRecognizerTest {
    private fun paragraphs(source: String) =
        BlockScanLexer(
            source,
            listOf(NewlineRecognizer, ParagraphRecognizer(BaseInterruptions.paragraph), BlockTextRecognizer),
        ).tokenize().filterIsInstance<ParagraphToken>().map { it.data.text }.toList()

    @Test
    fun `joins consecutive non-blank lines`() {
        assertEquals(listOf("a\nb\nc"), paragraphs("a\nb\nc"))
        assertEquals(listOf("a", "b"), paragraphs("a\n\nb"))
    }

    @Test
    fun `stops before an interruption but not before a space-indented continuation`() {
        assertEquals("a", paragraphs("a\n# h").first())
        assertEquals("a", paragraphs("a\n#! h").first())
        assertEquals("a\n10. x", paragraphs("a\n10. x").first())
        assertEquals("a", paragraphs("a\n1. x").first())
    }

    @Test
    fun `stops on any blank line but continues across an indented one`() {
        assertEquals(listOf("a", "b"), paragraphs("a\n\nb"))
        assertEquals(listOf("a", "b"), paragraphs("a\n   \nb"))
        assertEquals(listOf("a", "b"), paragraphs("a\n\t\nb"))
        assertEquals(listOf("a\n\tb"), paragraphs("a\n\tb"))
        assertEquals(listOf("a\n  b"), paragraphs("a\n  b"))
    }
}
