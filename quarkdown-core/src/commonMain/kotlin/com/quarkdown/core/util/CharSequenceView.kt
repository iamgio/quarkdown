package com.quarkdown.core.util

/**
 * A window over a range of another [CharSequence] that shares its characters instead of copying them.
 * The view is only materialized by [toString].
 * @param base the sequence this is a window of, never a [CharSequenceView] itself
 * @param start inclusive start index within [base]
 * @param end exclusive end index within [base]
 */
class CharSequenceView private constructor(
    private val base: CharSequence,
    private val start: Int,
    private val end: Int,
) : CharSequence {
    override val length: Int
        get() = end - start

    override fun get(index: Int): Char {
        if (index !in 0..<length) throw IndexOutOfBoundsException("Index $index out of bounds for length $length")
        return base[start + index]
    }

    override fun subSequence(
        startIndex: Int,
        endIndex: Int,
    ): CharSequence = of(this, startIndex, endIndex)

    override fun toString(): String = base.subSequence(start, end).toString()

    companion object {
        /**
         * Creates a view over `[start, end)` of [base], flattening it when [base] is itself a view.
         * @throws IndexOutOfBoundsException if the range does not fit in [base]
         */
        fun of(
            base: CharSequence,
            start: Int,
            end: Int,
        ): CharSequenceView =
            when {
                start < 0 || end > base.length || start > end -> {
                    throw IndexOutOfBoundsException("Range $start..<$end out of bounds for length ${base.length}")
                }

                base is CharSequenceView -> {
                    of(base.base, base.start + start, base.start + end)
                }

                else -> {
                    CharSequenceView(base, start, end)
                }
            }
    }
}

/**
 * @param start inclusive start index
 * @param end exclusive end index, defaulting to the end of this sequence
 * @return a view over `[start, end)` of this sequence that shares its characters rather than copying them
 * @see CharSequenceView
 */
fun CharSequence.view(
    start: Int,
    end: Int = length,
): CharSequence = CharSequenceView.of(this, start, end)
