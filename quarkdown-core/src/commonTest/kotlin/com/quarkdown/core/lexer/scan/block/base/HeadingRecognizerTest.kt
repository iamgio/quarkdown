package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.HeadingToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HeadingRecognizerTest {
    private fun line(text: String) = Lines.of("$text\n").first()

    private fun heading(source: String): HeadingToken? {
        val match = HeadingRecognizer.open(LineCursor(Lines.of("$source\n"), 0)) ?: return null
        return match.token as HeadingToken
    }

    @Test
    fun `reads depth, decorative marker, content and custom id`() {
        val plain = heading("## Title")!!
        assertEquals(2, plain.depth)
        assertEquals(" Title", plain.content)
        assertNull(plain.customId)
        assertTrue(heading("#! Title")!!.isDecorative)
        assertEquals("i", heading("# Title {#i}")!!.customId)
        assertEquals(" Title", heading("# Title {#i}")!!.content)
    }

    @Test
    fun `strips trailing hashes`() {
        assertEquals(" H", heading("### H ###")!!.content)
        assertEquals(" H", heading("#### H####")!!.content)
        assertEquals(" H #a", heading("# H #a#")!!.content)
        assertEquals("", heading("#")!!.content)
    }

    @Test
    fun `absorbs every blank line that follows`() {
        assertEquals("# H\n\n", heading("# H\n\nP")!!.data.text)
        assertEquals("# H\n   \n", heading("# H\n   \nP")!!.data.text)
        assertEquals("# H\n", heading("# H\nP")!!.data.text)
    }

    @Test
    fun `declines non-headings`() {
        listOf("#######  x", "#h", "    # h", "text").forEach { assertNull(heading(it), it) }
    }

    @Test
    fun `tells a heading opener from a lookalike`() {
        listOf("#! D", "### H", "#").forEach { assertTrue(HeadingRecognizer.opensAt(line(it)), it) }
        // Only ASCII whitespace separates the hashes from the text, so a no-break space does not.
        listOf("#######", "#h", "    # h", "#\u00a0h").forEach { assertFalse(HeadingRecognizer.opensAt(line(it)), it) }
    }
}
