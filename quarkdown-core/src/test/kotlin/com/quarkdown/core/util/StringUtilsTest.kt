package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Tests for string utilities.
 */
class StringUtilsTest {
    @Test
    fun `crlf separators are normalized to lf`() {
        assertEquals("a\nb\nc", "a\r\nb\r\nc".normalizeLineSeparators().toString())
    }

    @Test
    fun `lone cr separators are normalized to lf`() {
        assertEquals("a\nb\nc", "a\rb\rc".normalizeLineSeparators().toString())
    }

    @Test
    fun `mixed separators are normalized to lf`() {
        assertEquals("a\nb\nc\nd", "a\r\nb\rc\nd".normalizeLineSeparators().toString())
    }

    @Test
    fun `lf-only content is returned as-is`() {
        val text: CharSequence = "a\nb\nc"
        assertSame(text, text.normalizeLineSeparators())
    }
}
