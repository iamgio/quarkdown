package com.quarkdown.stdlib.internal

/**
 * A sorting strategy for sequences of elements.
 * @param T the type of elements to sort
 */
interface Sorting<T> {
    /**
     * Sorts a sequence of elements according to this strategy.
     */
    val sort: (Sequence<T>, Ordering) -> Sequence<T>
}

/**
 * The order in which elements are sorted.
 */
enum class Ordering {
    /** Elements are sorted in ascending order. */
    ASCENDING,

    /** Elements are sorted in descending order. */
    DESCENDING,
}

/**
 * Sorts elements by a [selector] value in the given [ordering].
 * @param ordering whether to sort in ascending or descending order
 * @param selector function that extracts a comparable value from each element
 * @return a new sequence with elements sorted according to the selector and ordering
 */
fun <T, R : Comparable<R>> Sequence<T>.sortedBy(
    ordering: Ordering,
    selector: (T) -> R?,
): Sequence<T> =
    when (ordering) {
        Ordering.ASCENDING -> this.sortedBy(selector)
        Ordering.DESCENDING -> this.sortedByDescending(selector)
    }
