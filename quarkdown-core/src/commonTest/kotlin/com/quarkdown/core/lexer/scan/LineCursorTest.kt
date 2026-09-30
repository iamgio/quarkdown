package com.quarkdown.core.lexer.scan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LineCursorTest {
    private fun cursor(
        source: String,
        index: Int = 0,
    ) = LineCursor(Lines.of(source), index)

    @Test
    fun `peeks within bounds and returns null past the end`() {
        val cursor = cursor("a\nb\nc")
        assertEquals("a", cursor.current.text)
        assertEquals("b", cursor.peek(1)?.text)
        assertEquals("c", cursor.peek(2)?.text)
        assertNull(cursor.peek(3))
        assertNull(cursor.peek(-1))
    }

    @Test
    fun `reports remaining lines`() {
        assertEquals(3, cursor("a\nb\nc").remaining)
        assertEquals(1, cursor("a\nb\nc", index = 2).remaining)
    }

    @Test
    fun `spans line counts including terminators`() {
        val cursor = cursor("ab\ncd\nef")
        assertEquals(3, cursor.spanTo(1))
        assertEquals(6, cursor.spanTo(2))
        assertEquals(8, cursor.spanTo(3))
    }
}
