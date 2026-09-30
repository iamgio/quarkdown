package com.quarkdown.core.lexer.scan.block.base

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.LinkDefinitionToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkDefinitionRecognizerTest {
    private fun definition(source: String) = LinkDefinitionRecognizer.open(LineCursor(Lines.of(source), 0))?.token as LinkDefinitionToken?

    @Test
    fun `reads label, url and optional title`() {
        val full = definition("[l]: <u> 'ti'\n")!!
        assertEquals("l", full.label)
        assertEquals("<u>", full.url)
        assertEquals("'ti'", full.title)
        assertNull(definition("[l]: u\n")!!.title)
    }

    @Test
    fun `accepts a url or title on the following line`() {
        assertEquals("u", definition("[l]:\n  u\n")!!.url)
        assertEquals("[l]:\n  u\n", definition("[l]:\n  u\n")!!.data.text)
        assertEquals("\"ti\"", definition("[l]: u\n  \"ti\"\n")!!.title)
    }

    @Test
    fun `leaves footnote labels to the footnote recognizer, which comes first`() {
        assertEquals("^l", definition("[^l]: u\n")!!.label)
        assertEquals("^", definition("[^]: x\n")!!.label)
    }

    @Test
    fun `spans lines in a label but declines one that spans a blank line`() {
        assertEquals("a\nb", definition("[a\nb]: u\n")!!.label)
        assertEquals("foo\n", definition("[foo\n]: u\n")!!.label)
        assertNull(definition("[\n\nfoo]: u\n"))
        assertNull(definition("[a\n  \nb]: u\n"))
    }

    @Test
    fun `keeps an escaped angle bracket inside an angle url and rejects an unescaped one`() {
        assertEquals("<a\\>b>", definition("[l]: <a\\>b>\n")!!.url)
        assertEquals("<a b>", definition("[l]: <a b>\n")!!.url)
        assertNull(definition("[l]: <a<b>\n"))
        assertNull(definition("[l]: <a\n"))
    }

    @Test
    fun `declines a title that spans a blank line`() {
        assertEquals("'a\nb'", definition("[l]: u 'a\nb'\n")!!.title)
        assertNull(definition("[l]: u 'a\n\nb'\n"))
    }

    @Test
    fun `reads a bare url as an inline link does, parentheses balanced`() {
        assertEquals("/a(b)c", definition("[l]: /a(b)c\n")!!.url)
        assertNull(definition("[l]: /path)\n"))
        assertNull(definition("[l]: /a(b\n"))
        assertEquals("foo\\(bar", definition("[l]: foo\\(bar\n")!!.url)
        assertNull(definition("[l]: foo\\ bar\n"))
    }

    @Test
    fun `declines when anything but spaces follows the definition`() {
        assertNull(definition("[l]: u rest\n"))
        assertNull(definition("[l]: u \"ti\" rest\n"))
        assertEquals("[l]: u  \n", definition("[l]: u  \n")?.data?.text)
    }

    @Test
    fun `keeps a definition whose next line is not a title, leaving that line out`() {
        val definition = definition("[l]: u\n\"ti\" rest\n")!!
        assertNull(definition.title)
        assertEquals("[l]: u\n", definition.data.text)
    }

    @Test
    fun `absorbs following empty lines and declines non-definitions`() {
        assertEquals("[l]: u\n\n", definition("[l]: u\n\nP\n")?.data?.text)
        assertNull(definition("[l] u\n"))
        assertNull(definition("    [l]: u\n"))
    }
}
