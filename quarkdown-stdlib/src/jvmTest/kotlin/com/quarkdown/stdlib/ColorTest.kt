package com.quarkdown.stdlib

import com.quarkdown.core.misc.color.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * [Color] module tests.
 */
class ColorTest {
    private val base = Color(100, 150, 200, 0.5)

    @Test
    fun `color from base without overrides keeps the color`() {
        assertEquals(base, color(base).unwrappedValue)
    }

    @Test
    fun `color from base overrides only the given components`() {
        assertEquals(Color(255, 150, 200, 0.5), color(base, red = 255).unwrappedValue)
        assertEquals(Color(100, 150, 200, 1.0), color(base, alpha = 1).unwrappedValue)
        assertEquals(Color(0, 10, 20, 0.25), color(base, red = 0, green = 10, blue = 20, alpha = 0.25).unwrappedValue)
    }

    @Test
    fun `color without base starts from opaque black`() {
        assertEquals(Color(0, 0, 0, 1.0), color().unwrappedValue)
        assertEquals(Color(255, 0, 0, 1.0), color(red = 255).unwrappedValue)
        assertEquals(Color(0, 0, 0, 0.0), color(alpha = 0).unwrappedValue)
    }

    @Test
    fun `color rejects out-of-range components`() {
        assertFailsWith<IllegalArgumentException> { color(base, red = 256) }
        assertFailsWith<IllegalArgumentException> { color(base, green = -1) }
        assertFailsWith<IllegalArgumentException> { color(base, alpha = 1.5) }
        assertFailsWith<IllegalArgumentException> { color(base, alpha = -0.1) }
    }
}
