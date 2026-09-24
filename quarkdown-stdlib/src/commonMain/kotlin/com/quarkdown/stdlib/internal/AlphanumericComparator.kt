package com.quarkdown.stdlib.internal

/**
 * A comparator that sorts alphanumeric strings in a human-friendly way,
 * also known as *natural sort*: runs of digits are compared by their numeric value
 * instead of character by character.
 * For example, `$120` comes after `$30`, as opposed to the usual lexicographical order.
 *
 * Strings are split into chunks of consecutive digits or non-digits, compared pairwise:
 * - Two digit chunks are compared by numeric value. Equal values with different
 *   zero-padding (e.g. `7` vs `07`) are only ordered, fewer leading zeros first,
 *   when the strings are otherwise equal.
 * - Any other pair of chunks is compared lexicographically.
 *
 * This is a multiplatform port of [sawano/alphanumeric-comparator](https://github.com/sawano/alphanumeric-comparator).
 */
object AlphanumericComparator : Comparator<CharSequence> {
    override fun compare(
        a: CharSequence,
        b: CharSequence,
    ): Int {
        var i = 0
        var j = 0
        var zeroPaddingTiebreak = 0

        while (i < a.length && j < b.length) {
            val endA = a.chunkEnd(i)
            val endB = b.chunkEnd(j)

            val result =
                if (a[i].isDigit() && b[j].isDigit()) {
                    val numeric = compareNumerically(a, i, endA, b, j, endB)
                    if (numeric == 0 && zeroPaddingTiebreak == 0) {
                        zeroPaddingTiebreak = endA - i - (endB - j)
                    }
                    numeric
                } else {
                    compareLexicographically(a, i, endA, b, j, endB)
                }

            if (result != 0) return result
            i = endA
            j = endB
        }

        // The string with remaining content comes last; otherwise zero-padding decides.
        val remaining = a.length - i - (b.length - j)
        return if (remaining != 0) remaining else zeroPaddingTiebreak
    }

    /**
     * @return the end index (exclusive) of the chunk starting at [start],
     *         i.e. the longest run of characters that are all digits or all non-digits
     */
    private fun CharSequence.chunkEnd(start: Int): Int {
        val isDigitChunk = this[start].isDigit()
        var end = start + 1
        while (end < length && this[end].isDigit() == isDigitChunk) end++
        return end
    }

    /**
     * Compares two digit chunks by numeric value:
     * leading zeros are skipped, then a longer number is greater,
     * and equally long numbers are compared digit by digit.
     */
    private fun compareNumerically(
        a: CharSequence,
        startA: Int,
        endA: Int,
        b: CharSequence,
        startB: Int,
        endB: Int,
    ): Int {
        var i = startA
        var j = startB
        while (i < endA - 1 && a[i].digitToInt() == 0) i++
        while (j < endB - 1 && b[j].digitToInt() == 0) j++

        val lengthDifference = endA - i - (endB - j)
        if (lengthDifference != 0) return lengthDifference

        while (i < endA) {
            val result = a[i].digitToInt() - b[j].digitToInt()
            if (result != 0) return result
            i++
            j++
        }
        return 0
    }

    /**
     * Compares two chunks character by character, with a shorter chunk coming first.
     */
    private fun compareLexicographically(
        a: CharSequence,
        startA: Int,
        endA: Int,
        b: CharSequence,
        startB: Int,
        endB: Int,
    ): Int {
        var i = startA
        var j = startB
        while (i < endA && j < endB) {
            val result = a[i].compareTo(b[j])
            if (result != 0) return result
            i++
            j++
        }
        return endA - startA - (endB - startB)
    }
}
