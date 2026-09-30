package com.quarkdown.core.lexer.scan.block.quarkdown

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.Lines
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FunctionCallRecognizerTest {
    private fun call(source: String) = FunctionCallRecognizer.open(LineCursor(Lines.of(source), 0))

    @Test
    fun `produces a zero-width token and reports the walker's end`() {
        val match = call(".func {x}\n")!!
        val token = match.token as FunctionCallToken
        assertEquals("", token.data.text)
        assertTrue(token.data.position.isEmpty())
        assertTrue(token.isBlock)
        assertEquals(9, match.scanEnd)
    }

    @Test
    fun `accepts a body and a chain, and declines an inline call`() {
        assertEquals(19, call(".func {x}\n    body\npara\n")!!.scanEnd)
        assertTrue(call(".func {x}::chain {y}\n") != null)
        assertTrue(call("{.func {x}}\n") != null)
        assertNull(call(".func {x} trailing\n"))
        assertNull(call("para\n"))
    }
}
