package com.quarkdown.stdlib.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [AlphanumericComparator] ("natural sort").
 */
class AlphanumericComparatorTest {
    private fun assertOrder(vararg expected: String) {
        assertEquals(
            expected.toList(),
            expected.toList().reversed().sortedWith(AlphanumericComparator),
        )
    }

    private fun assertBefore(
        first: String,
        second: String,
    ) {
        assertTrue(
            AlphanumericComparator.compare(first, second) < 0,
            "Expected \"$first\" < \"$second\"",
        )
        assertTrue(
            AlphanumericComparator.compare(second, first) > 0,
            "Expected \"$second\" > \"$first\"",
        )
    }

    @Test
    fun `numbers are compared by value`() {
        assertOrder("item1", "item2", "item10", "item20", "item100")
        assertOrder("$5", "$30", "$120", "$1000")
        assertOrder("12", "123")
    }

    @Test
    fun `plain text is compared lexicographically`() {
        assertOrder("apple", "banana", "pie")
        assertBefore("A", "a")
        assertBefore("B", "a")
    }

    @Test
    fun `shorter prefix comes first`() {
        assertBefore("a", "a1")
        assertBefore("", "a")
    }

    @Test
    fun `digits come before letters`() {
        assertBefore("1", "a")
    }

    @Test
    fun `version-like strings`() {
        assertOrder("v1.2", "v1.5", "v1.10", "v2.1")
    }

    @Test
    fun `equal numeric values with fewer leading zeros come first`() {
        assertOrder("7", "07", "007", "70", "0070")
        assertBefore("a1", "a01")
        assertBefore("a01", "a001")
    }

    @Test
    fun `leading zeros are only a last-resort tiebreak`() {
        // The 'b' vs 'c' comparison wins over the "01" vs "1" padding difference.
        assertBefore("a01b", "a1c")
    }

    @Test
    fun `text chunks are compared as a whole`() {
        // The text chunk "a " is longer than "a", which precedes the ' ' vs '1' character comparison.
        assertBefore("a1", "a 1")
    }

    @Test
    fun `unicode digits are compared by numeric value`() {
        // Arabic-Indic digits: ٢ = 2, ٩ = 9, ٣٠ = 30, ١٢٠ = 120.
        assertBefore("٢", "3")
        assertBefore("٩", "10")
        assertOrder("$٣٠", "$١٢٠")
        // Devanagari digits in file names: १ = 1, २ = 2, १० = 10.
        assertOrder("img१.png", "img२.png", "img१०.png")
    }

    @Test
    fun `unicode leading zeros are trimmed`() {
        // ٠ is the Arabic-Indic zero: ٧ = 7, ٠٠7 = 007.
        assertOrder("٧", "07", "٠٠7", "70")
        // The 'b' vs 'c' comparison wins over the padding difference, as with ASCII zeros.
        assertBefore("a٠١b", "a1c")
    }

    @Test
    fun `equal strings compare as equal`() {
        assertEquals(0, AlphanumericComparator.compare("item10", "item10"))
        assertEquals(0, AlphanumericComparator.compare("", ""))
    }
}
