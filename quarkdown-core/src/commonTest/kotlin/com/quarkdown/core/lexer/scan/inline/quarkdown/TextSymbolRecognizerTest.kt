package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.tokens.TextSymbolToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextSymbolRecognizerTest {
    private fun symbol(
        replacement: TextSymbolReplacement,
        source: String,
        index: Int = 0,
    ) = replacement.toRecognizer().recognize("$source\n", index)?.token as TextSymbolToken?

    @Test
    fun `matches the case-insensitive literals`() {
        assertEquals('©', symbol(TextSymbolReplacement.COPYRIGHT, "(C)")!!.symbol.result)
        assertEquals('©', symbol(TextSymbolReplacement.COPYRIGHT, "(c)")!!.symbol.result)
        assertEquals("(tm)", symbol(TextSymbolReplacement.TRADEMARK, "(tm)")!!.data.text)
        assertEquals("--", symbol(TextSymbolReplacement.EM_DASH, "--")!!.data.text)
    }

    @Test
    fun `applies the contextual conditions`() {
        assertEquals("-", symbol(TextSymbolReplacement.EN_DASH, "a - b", index = 2)!!.data.text)
        assertNull(symbol(TextSymbolReplacement.EN_DASH, "a-b", index = 1))
        assertEquals("...", symbol(TextSymbolReplacement.ELLIPSIS, "a...", index = 1)!!.data.text)
        assertEquals("...", symbol(TextSymbolReplacement.ELLIPSIS, "... a")!!.data.text)
        assertNull(symbol(TextSymbolReplacement.ELLIPSIS, "a...b", index = 1))
        assertNull(symbol(TextSymbolReplacement.TYPOGRAPHIC_LEFT_APOSTROPHE, "don't", index = 3))
        assertEquals("'", symbol(TextSymbolReplacement.TYPOGRAPHIC_RIGHT_APOSTROPHE, "don't", index = 3)!!.data.text)
    }
}
