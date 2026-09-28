package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.tokens.InlineMathToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InlineMathRecognizerTest {
    private fun math(
        source: String,
        index: Int = 0,
    ) = InlineMathRecognizer.recognize("$source\n", index)?.token as InlineMathToken?

    @Test
    fun `reads an isolated expression`() {
        assertEquals("x", math("$ x $")!!.expression)
        assertEquals("x", math("a $ x $ b", index = 2)!!.expression)
        assertEquals("a\$b", math("$ a\$b $")!!.expression)
    }

    @Test
    fun `requires a boundary on both sides`() {
        assertNull(math("a$ x $", index = 1))
        assertNull(math("\$x$"))
        assertNull(math("$ x \$b"))
    }
}
