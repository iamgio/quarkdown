package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.tokens.EntityToken
import com.quarkdown.core.lexer.tokens.EscapeToken
import com.quarkdown.core.lexer.tokens.LineBreakToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SimpleInlineRecognizersTest {
    @Test
    fun `escapes one punctuation character`() {
        assertEquals("#", (EscapeRecognizer.recognize("\\#x\n", 0)!!.token as EscapeToken).character)
        assertNull(EscapeRecognizer.recognize("\\a\n", 0))
        assertNull(EscapeRecognizer.recognize("a\n", 0))
    }

    @Test
    fun `reads named, decimal and hexadecimal entities in either case`() {
        assertEquals("copy", (EntityRecognizer.recognize("&copy;\n", 0)!!.token as EntityToken).body)
        assertEquals("35", (EntityRecognizer.recognize("&#35;\n", 0)!!.token as EntityToken).numeric)
        assertEquals("22", (EntityRecognizer.recognize("&#x22;\n", 0)!!.token as EntityToken).numeric)
        assertEquals("22", (EntityRecognizer.recognize("&#X22;\n", 0)!!.token as EntityToken).numeric)
        assertEquals(
            "&nbsp",
            EntityRecognizer
                .recognize("&nbsp\n", 0)!!
                .token.data.text,
        )
    }

    @Test
    fun `tells hard breaks from soft ones and ignores a trailing newline`() {
        assertTrue((LineBreakRecognizer.recognize("a  \nb\n", 1)!!.token as LineBreakToken).isHard)
        assertTrue((LineBreakRecognizer.recognize("a\\\nb\n", 1)!!.token as LineBreakToken).isHard)
        assertFalse((LineBreakRecognizer.recognize("a\nb\n", 1)!!.token as LineBreakToken).isHard)
        assertNull(LineBreakRecognizer.recognize("a\n\n", 1))
    }
}
