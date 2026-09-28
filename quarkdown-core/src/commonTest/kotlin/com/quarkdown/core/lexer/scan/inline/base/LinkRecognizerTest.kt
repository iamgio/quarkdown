package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.tokens.DiamondAutolinkToken
import com.quarkdown.core.lexer.tokens.LinkToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkRecognizerTest {
    private fun link(source: String) = LinkRecognizer.recognize("$source\n", 0)?.token as LinkToken?

    @Test
    fun `reads label, url and title`() {
        val full = link("[l](u \"t\")")!!
        assertEquals("l", full.label)
        assertEquals("u", full.url)
        assertEquals("\"t\"", full.title)
        assertNull(link("[l](u)")!!.title)
        assertNull(link("[l]"))
    }

    @Test
    fun `needs an angle url to close, and no escaped space in a bare one`() {
        assertEquals("<u>", link("[l](<u>)")!!.url)
        assertNull(link("[l](<u)"))
        assertEquals("a\\(b", link("[l](a\\(b)")!!.url)
        assertNull(link("[l](a\\ b)"))
    }

    @Test
    fun `reads both autolink forms and respects the case-insensitive scheme`() {
        assertEquals("http://x.com", (DiamondAutolinkRecognizer.recognize("<http://x.com>\n", 0)!!.token as DiamondAutolinkToken).url)
        assertEquals("a@b.com", (DiamondAutolinkRecognizer.recognize("<a@b.com>\n", 0)!!.token as DiamondAutolinkToken).url)
        assertEquals(
            "http://x.com",
            UrlAutolinkRecognizer
                .recognize("http://x.com\n", 0)!!
                .token.data.text,
        )
        assertEquals(
            "HTTP://x.com",
            UrlAutolinkRecognizer
                .recognize("HTTP://x.com\n", 0)!!
                .token.data.text,
        )
        assertEquals(
            "www.x.com",
            UrlAutolinkRecognizer
                .recognize("www.x.com\n", 0)!!
                .token.data.text,
        )
        assertNull(UrlAutolinkRecognizer.recognize("x.com\n", 0))
    }
}
