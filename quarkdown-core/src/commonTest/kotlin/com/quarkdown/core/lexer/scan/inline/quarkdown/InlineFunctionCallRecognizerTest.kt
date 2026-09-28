package com.quarkdown.core.lexer.scan.inline.quarkdown

import com.quarkdown.core.lexer.tokens.FunctionCallToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InlineFunctionCallRecognizerTest {
    private fun call(
        source: String,
        index: Int = 0,
    ) = InlineFunctionCallRecognizer.recognize("$source\n", index)

    @Test
    fun `produces a zero-width token whose end comes from the walker`() {
        val match = call(".f {x}")!!
        assertEquals("", match.token.data.text)
        assertTrue(
            match.token.data.position
                .isEmpty(),
        )
        assertFalse((match.token as FunctionCallToken).isBlock)
        assertEquals(6, match.end)
    }

    @Test
    fun `respects the preceding character rule`() {
        assertEquals(8, call("a .f {x}", index = 2)!!.end)
        assertNull(call("a.f {x}", index = 1))
        assertNull(call("\\.f {x}", index = 1))
        assertNull(call("text", index = 0))
    }
}
