package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.MultilineMathToken
import com.quarkdown.core.lexer.tokens.OnelineMathToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MathRecognizerTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    private fun oneline(source: String) = OnelineMathRecognizer.open(LineCursor(Lines.of(source), 0))?.token as OnelineMathToken?

    private fun multiline(source: String) = MultilineMathRecognizer.open(LineCursor(Lines.of(source), 0))?.token as MultilineMathToken?

    @Test
    fun `reads one-line math and its custom id`() {
        assertEquals("x", oneline("$ x $\n")!!.expression)
        assertEquals("$ x $", oneline("$ x $\n")!!.data.text)
        assertEquals("i", oneline("$ x $ {#i}\n")!!.customId)
        assertEquals("a\$b", oneline("$ a\$b $\n")!!.expression)
    }

    @Test
    fun `reads multiline math and its custom id`() {
        assertEquals("\nx\n", multiline("$$$\nx\n$$$\n")!!.expression)
        assertEquals("i", multiline("$$$ {#i}\nx\n$$$\n")!!.customId)
        assertEquals("$$$\nx\n$$$", multiline("$$$\nx\n$$$\n")!!.data.text)
    }

    @Test
    fun `declines malformed math`() {
        assertNull(oneline("\$x$\n"))
        assertNull(oneline("$ x $ trailing\n"))
        assertNull(multiline("$$$\nx\n"))
        assertNull(multiline("$$\nx\n$$\n"))
    }

    @Test
    fun `tells a multiline math opener from a lookalike`() {
        assertTrue(MultilineMathRecognizer.opensAt(line("$$$ {#i}")))
        assertFalse(MultilineMathRecognizer.opensAt(line("$$")))
    }
}
