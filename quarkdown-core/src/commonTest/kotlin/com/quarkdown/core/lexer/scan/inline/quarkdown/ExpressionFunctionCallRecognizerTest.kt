package com.quarkdown.core.lexer.scan.inline.quarkdown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExpressionFunctionCallRecognizerTest {
    private fun call(
        source: String,
        index: Int = 0,
    ) = ExpressionFunctionCallRecognizer.recognize("$source\n", index)

    @Test
    fun `matches at a line start and always produces a token`() {
        assertEquals("", call(".f {x}")!!.token.data.text)
        assertEquals(6, call(".f {x}")!!.end)
        assertEquals(6, call(".f {x} more")!!.end)
        assertEquals("  ", call("  .f {x}")!!.token.data.text)
    }

    @Test
    fun `declines away from a line start`() {
        assertNull(call("a .f {x}", index = 2))
        assertNull(call("     .f {x}"))
        assertNull(call("text"))
    }
}
