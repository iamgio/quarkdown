package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for [CharSequenceView].
 */
class CharSequenceViewTest {
    private val view = "abcdef".view(1, 4) // "bcd"

    @Test
    fun `exposes the window of the base`() {
        assertEquals(3, view.length)
        assertEquals('b', view[0])
        assertEquals('d', view[2])
        assertEquals("bcd", view.toString())
    }

    @Test
    fun `sub-views stay flat over the base`() {
        val sub = view.subSequence(1, 3)
        assertEquals("cd", sub.toString())
        assertEquals("d", sub.subSequence(1, 2).toString())
    }

    @Test
    fun `rejects indices outside the window`() {
        assertFailsWith<IndexOutOfBoundsException> { view[3] }
        assertFailsWith<IndexOutOfBoundsException> { view[-1] }
    }

    @Test
    fun `rejects sub-view ranges outside the window even when inside the base`() {
        assertFailsWith<IndexOutOfBoundsException> { view.subSequence(0, 5) }
        assertFailsWith<IndexOutOfBoundsException> { view.subSequence(-1, 2) }
        assertFailsWith<IndexOutOfBoundsException> { view.subSequence(2, 1) }
    }

    @Test
    fun `rejects ranges outside the base`() {
        assertFailsWith<IndexOutOfBoundsException> { "abc".view(0, 4) }
        assertFailsWith<IndexOutOfBoundsException> { "abc".view(2, 1) }
    }
}
