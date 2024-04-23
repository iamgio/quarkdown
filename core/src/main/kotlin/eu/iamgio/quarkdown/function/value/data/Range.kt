package eu.iamgio.quarkdown.function.value.data

import eu.iamgio.quarkdown.function.value.NumberValue

/**
 * Represents a range of numbers, which can also be iterated through.
 * @property start start of the range (inclusive). If `null`, the range is infinite on the left end
 * @property end end of the range (inclusive). If `null`, the range is infinite on the right end
 */
data class Range(val start: Int?, val end: Int?) : Iterable<NumberValue> {
    /**
     * @return whether the range is infinite, i.e. both [start] and [end] are `null`
     */
    val isInfinite: Boolean
        get() = start == null && end == null

    /**
     * @param lowerBound lower bound of the range, in case [start] is `null`
     * @param upperBound upper bound of the range, in case [end] is `null`
     * @return this range as an [IntRange], with [start] and [end] replaced by [lowerBound] and [upperBound] respectively if they are `null`
     */
    fun toIntRange(
        lowerBound: Int,
        upperBound: Int,
    ) = IntRange(start ?: lowerBound, end ?: upperBound)

    /**
     * @return a new iterator for this range.
     * If this is open on the left end, it starts from 0.
     * If this is open on the right end, it ends at [Int.MAX_VALUE].
     */
    override fun iterator(): Iterator<NumberValue> =
        toIntRange(lowerBound = 0, upperBound = Int.MAX_VALUE).asSequence()
            .map(::NumberValue)
            .iterator()

    override fun toString() = "${start ?: ""}..${end ?: ""}"

    companion object {
        /**
         * An infinite range on both ends.
         */
        val INFINITE = Range(null, null)
    }
}

/**
 * @param range range to get the sublist from
 * @return sublist of [this] list, starting from [range]'s start and ending at [range]'s end (both inclusive).
 *         If any of the bounds is `null`, it is replaced by the list's start or end index respectively
 */
fun <T> List<T>.subList(range: Range): List<T> {
    return subList(range.start ?: 0, range.end?.plus(1) ?: this.size)
}
