package eu.iamgio.quarkdown.document.numbering

/**
 * Represents a format that defines how items (e.g. headings) are numbered in a document,
 * depending on their relative position and level of nesting.
 * For example, a format `1.A.a` would result in the following numbering:
 * - 1
 *  - 1.A
 *  - 1.B
 *    - 1.B.a
 * - 2
 *   - 2.A
 *     - 2.A.a
 *   - 2.B
 * In this example, the format consists of the following symbols:
 * - `1` is a [DecimalNumberingSymbol], which counts `1, 2, 3, ...`
 * - `.` is a [NumberingFixedSymbol]
 * - `A` is an [UppercaseAlphaNumberingSymbol], which counts `A, B, C, ...`
 * - `.` is a [NumberingFixedSymbol]
 * - `a` is a [LowercaseAlphaNumberingSymbol], which counts `a, b, c, ...`
 * A format can be imported and exported as a string via [fromString] and [format] respectively.
 * @param symbols ordered list of symbols that define the format
 * @see NumberingSymbol
 */
data class NumberingFormat(
    val symbols: List<NumberingSymbol>,
) {
    /**
     * Converts the numbering format into a string.
     * For example, the [NumberingFormat] `1.A.a` would format the levels `1, 1, 0` as `2.B.a`.
     *
     * In case [levels] and [symbols] have different lengths, the output will be truncated to the shortest of the two:
     * for example, `1.A.a` formats the levels `1, 1, 0, 0` as `2.B.a`, and the levels `1, 1` as `2.B`.
     *
     * @param levels nesting levels to format.
     * For example, when it comes to numbering headings, `1, 1, 0` corresponds to:
     * ```markdown
     * # A
     * ## A.A
     * # B
     * ## B.A
     * ## B.B
     * ### B.B.A <-- This is the target level!
     * # C
     * ```
     * @return the formatted string
     */
    fun format(vararg levels: Int): String {
        val levelsIterator = levels.iterator()

        return symbols.joinToString(separator = "") { symbol ->
            // Case levels.length < symbols.length: ignore the remaining symbols.
            if (!levelsIterator.hasNext()) return@joinToString ""

            // Appending the corresponding symbol.
            when (symbol) {
                // The counter maps the nesting level to a string.
                is NumberingCounterSymbol -> symbol.map(levelsIterator.next())
                // Fixed symbols are directly appended as-is.
                is NumberingFixedSymbol -> symbol.value.toString()
            }
        }
    }

    companion object {
        /**
         * Parses a [NumberingFormat] from a string,
         * where each character represents a symbol.
         * `1`, `a`, `A`, `i` and `I` are reserved for counting,
         * while any other character is considered a fixed symbol.
         * @param string string to parse
         * @return parsed numbering format
         */
        fun fromString(string: String): NumberingFormat {
            val symbols =
                string.map {
                    when (it) {
                        '1' -> DecimalNumberingSymbol()
                        'a' -> LowercaseAlphaNumberingSymbol()
                        'A' -> UppercaseAlphaNumberingSymbol()
                        'i' -> TODO("Roman numbering is not yet implemented")
                        'I' -> TODO("Roman numbering is not yet implemented")
                        else -> NumberingFixedSymbol(it)
                    }
                }
            return NumberingFormat(symbols)
        }
    }
}
