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
 *
 * `null` values are always sorted first, regardless of the ordering.
 *
 * @param ordering whether to sort in ascending or descending order
 * @param comparator optional additional comparator to use for comparing the selected values
 * @param selector function that extracts a comparable value from each element
 * @return a new sequence with elements sorted according to the selector and ordering
 */
fun <T, R : Comparable<R>> Sequence<T>.sortedBy(
    ordering: Ordering,
    comparator: Comparator<in R>? = null,
    selector: (T) -> R?,
): Sequence<T> {
    val nullSafeComparator = comparator?.let(::nullsFirst) ?: nullsFirst()
    val finalComparator =
        when (ordering) {
            Ordering.ASCENDING -> compareBy(nullSafeComparator, selector)
            Ordering.DESCENDING -> compareByDescending(nullSafeComparator, selector)
        }
    return this.sortedWith(finalComparator)
}

/**
 * A comparator that sorts alphanumeric strings in a human-friendly way.
 * For example, `$120` comes after `$30`, as opposed to the usual lexicographical order.
 *
 * Wrapped around the `alphanumeric-comparator` library.
 */
object AlphanumericComparator : Comparator<CharSequence> by se.sawano.java.text
    .AlphanumericComparator()
