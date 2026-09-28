package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.tokens.ImageToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageRecognizersTest {
    private fun image(source: String) = ImageRecognizer.recognize("$source\n", 0)?.token as ImageToken?

    @Test
    fun `reads the delegated link and the optional size`() {
        val plain = image("![i](u)")!!
        assertEquals("i", plain.label)
        assertEquals("u", plain.url)
        assertNull(plain.width)
        val sized = image("!(2x3)[i](u)")!!
        assertEquals("2", sized.width)
        assertEquals("3", sized.height)
        assertEquals("!(2x3)[i](u)", sized.data.text)
    }

    @Test
    fun `accepts either case of the x divider`() {
        val upper = image("!(2X3)[i](u)")!!
        assertEquals("2", upper.width)
        assertEquals("3", upper.height)
        // The lookbehind is case free, so a unit ending in a letter still does not split.
        assertEquals("1cmX1cm", image("!(1cmX1cm)[i](u)")!!.width)
    }

    @Test
    fun `reads a width-only size and a custom id`() {
        assertEquals("2", image("!(2)[i](u)")!!.width)
        assertNull(image("!(2)[i](u)")!!.height)
        assertEquals("i2", image("![i](u){#i2}")!!.customId)
        assertNull(image("[i](u)"))
    }

    @Test
    fun `recognizes an inline comment`() {
        assertEquals(
            "<!-- c -->",
            InlineCommentRecognizer
                .recognize("<!-- c -->\n", 0)!!
                .token.data.text,
        )
        assertNull(InlineCommentRecognizer.recognize("<!- c ->\n", 0))
    }
}
