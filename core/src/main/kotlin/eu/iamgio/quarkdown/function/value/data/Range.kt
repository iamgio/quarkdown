package eu.iamgio.quarkdown.function.value.data

import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.IterableValue
import eu.iamgio.quarkdown.function.value.NumberValue

/**
 * Represents a range of numbers, which can also be iterated through.
 * @property start start of the range (inclusive). If `null`, the range is infinite on the left end
 * @property end end of the range (inclusive). If `null`, the range is infinite on the right end. [end] > [start]
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

    fun toCollection(): IterableValue<NumberValue> = GeneralCollectionValue(this)

    /**
     * @return a new iterator for this range.
     * If this is open on the left end, it starts from 0.
     * @throws IllegalStateException if [end] is `null`
     */
    override fun iterator(): Iterator<NumberValue> {
        if (end == null) {
            throw IllegalStateException("Cannot iterate through an endless range.")
        }

        return toIntRange(lowerBound = 0, upperBound = end).asSequence()
            .map(::NumberValue)
            .iterator()
    }

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
