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
 * @param symbols ordered list of symbols that define the format
 * @see NumberingSymbol
 */
data class NumberingFormat(
    val symbols: List<NumberingSymbol>,
) {
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
