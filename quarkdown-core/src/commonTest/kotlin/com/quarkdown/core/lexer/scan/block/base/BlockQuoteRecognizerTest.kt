package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.scan.block.BaseInterruptions
import com.quarkdown.core.lexer.tokens.BlockQuoteToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BlockQuoteRecognizerTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    private val recognizer = BlockQuoteRecognizer(BaseInterruptions.paragraph)

    private fun quote(source: String) = recognizer.open(LineCursor(Lines.of(source), 0))?.token as BlockQuoteToken?

    @Test
    fun `spans consecutive quote lines including the last terminator`() {
        assertEquals("> a\n> b\n", quote("> a\n> b\n")?.data?.text)
        assertEquals("a\nb", quote("> a\n> b\n")?.content)
        assertEquals("> d", quote(">> d\n")?.content)
    }

    @Test
    fun `keeps a lazy continuation but stops after an empty quote line`() {
        assertEquals("> q\nlazy\n", quote("> q\nlazy\n\n> q2\n")?.data?.text)
        assertEquals("> \n", quote("> \nlazy\n")?.data?.text)
        assertEquals(">\n", quote(">\nlazy\n")?.data?.text)
    }

    @Test
    fun `stops before an interruption and declines non-quotes`() {
        assertEquals("> q\n", quote("> q\n# h\n")?.data?.text)
        assertNull(quote("    > q\n"))
        assertNull(quote("text\n"))
    }

    @Test
    fun `tells a quote opener from a lookalike`() {
        assertTrue(BlockQuoteRecognizer.opensAt(line("> a")))
        assertTrue(BlockQuoteRecognizer.opensAt(line("   >a")))
        assertFalse(BlockQuoteRecognizer.opensAt(line("a > b")))
    }
}
