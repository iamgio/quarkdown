package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RomanNumerals] formatting.
 */
class RomanNumeralsTest {
    @Test
    fun `single symbols`() {
        assertEquals("I", RomanNumerals.format(1))
        assertEquals("V", RomanNumerals.format(5))
        assertEquals("X", RomanNumerals.format(10))
        assertEquals("L", RomanNumerals.format(50))
        assertEquals("C", RomanNumerals.format(100))
        assertEquals("D", RomanNumerals.format(500))
        assertEquals("M", RomanNumerals.format(1000))
    }

    @Test
    fun `subtractive forms`() {
        assertEquals("IV", RomanNumerals.format(4))
        assertEquals("IX", RomanNumerals.format(9))
        assertEquals("XL", RomanNumerals.format(40))
        assertEquals("XC", RomanNumerals.format(90))
        assertEquals("CD", RomanNumerals.format(400))
        assertEquals("CM", RomanNumerals.format(900))
    }

    @Test
    fun `composite values`() {
        assertEquals("III", RomanNumerals.format(3))
        assertEquals("XIV", RomanNumerals.format(14))
        assertEquals("XLIX", RomanNumerals.format(49))
        assertEquals("XCIX", RomanNumerals.format(99))
        assertEquals("DCCLXXXIX", RomanNumerals.format(789))
        assertEquals("MCMXCIV", RomanNumerals.format(1994))
        assertEquals("MMXXVI", RomanNumerals.format(2026))
    }

    @Test
    fun `range bounds`() {
        assertEquals("I", RomanNumerals.format(RomanNumerals.SUPPORTED_RANGE.first))
        assertEquals("MMMCMXCIX", RomanNumerals.format(RomanNumerals.SUPPORTED_RANGE.last))
    }
}
