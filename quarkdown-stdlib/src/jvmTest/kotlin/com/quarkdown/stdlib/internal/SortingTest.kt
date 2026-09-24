package com.quarkdown.stdlib.internal

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [Sorting] and [Ordering] tests.
 */
class SortingTest {
    @Test
    fun `sortedBy ascending`() {
        val sequence = sequenceOf(3, 1, 4, 1, 5, 9, 2, 6)
        val sorted = sequence.sortedBy(Ordering.ASCENDING) { it }
        assertEquals(listOf(1, 1, 2, 3, 4, 5, 6, 9), sorted.toList())
    }

    @Test
    fun `sortedBy descending`() {
        val sequence = sequenceOf(3, 1, 4, 1, 5, 9, 2, 6)
        val sorted = sequence.sortedBy(Ordering.DESCENDING) { it }
        assertEquals(listOf(9, 6, 5, 4, 3, 2, 1, 1), sorted.toList())
    }

    @Test
    fun `sortedBy with selector ascending`() {
        val sequence = sequenceOf("apple", "pie", "a", "banana")
        val sorted = sequence.sortedBy(Ordering.ASCENDING) { it.length }
        assertEquals(listOf("a", "pie", "apple", "banana"), sorted.toList())
    }

    @Test
    fun `sortedBy with selector descending`() {
        val sequence = sequenceOf("apple", "pie", "a", "banana")
        val sorted = sequence.sortedBy(Ordering.DESCENDING) { it.length }
        assertEquals(listOf("banana", "apple", "pie", "a"), sorted.toList())
    }

    @Test
    fun `sortedBy with null values`() {
        val sequence = sequenceOf("apple", "pie", null, "banana")
        val sorted = sequence.sortedBy(Ordering.ASCENDING) { it?.length }
        // Nulls come first when sorting ascending
        assertEquals(listOf(null, "pie", "apple", "banana"), sorted.toList())
    }

    @Test
    fun `sortedBy empty sequence`() {
        val sequence = emptySequence<Int>()
        val sortedAsc = sequence.sortedBy(Ordering.ASCENDING) { it }
        val sortedDesc = sequence.sortedBy(Ordering.DESCENDING) { it }
        assertEquals(emptyList(), sortedAsc.toList())
        assertEquals(emptyList(), sortedDesc.toList())
    }

    @Test
    fun `sortedBy with AlphanumericComparator`() {
        val sequence = sequenceOf("$120", "$30", "$5", "$1000")
        val sorted = sequence.sortedBy(Ordering.ASCENDING, AlphanumericComparator) { it }
        assertEquals(listOf("$5", "$30", "$120", "$1000"), sorted.toList())
    }

    @Test
    fun `sortedBy with AlphanumericComparator descending`() {
        val sequence = sequenceOf("item2", "item10", "item1", "item20")
        val sorted = sequence.sortedBy(Ordering.DESCENDING, AlphanumericComparator) { it }
        assertEquals(listOf("item20", "item10", "item2", "item1"), sorted.toList())
    }

    @Test
    fun `sorting interface implementation`() {
        val stringSorting =
            object : Sorting<String> {
                override val sort: (Sequence<String>, Ordering) -> Sequence<String> = { seq, ordering ->
                    seq.sortedBy(ordering) { it.length }
                }
            }

        val sequence = sequenceOf("apple", "pie", "a", "banana")

        assertEquals(
            listOf("a", "pie", "apple", "banana"),
            stringSorting.sort(sequence, Ordering.ASCENDING).toList(),
        )
        assertEquals(
            listOf("banana", "apple", "pie", "a"),
            stringSorting.sort(sequence, Ordering.DESCENDING).toList(),
        )
    }
}
