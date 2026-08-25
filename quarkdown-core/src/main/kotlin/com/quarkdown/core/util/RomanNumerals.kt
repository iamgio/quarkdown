package com.quarkdown.core.util

/**
 * Formatter of integers as Roman numerals.
 */
object RomanNumerals {
    /**
     * The range of values that can be represented as a Roman numeral.
     */
    val SUPPORTED_RANGE: IntRange = 1..3999

    /**
     * Roman numeral symbols, including subtractive forms, from the highest to the lowest value.
     */
    private val SYMBOLS =
        listOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I",
        )

    /**
     * Formats a value as an uppercase Roman numeral.
     * @param value value to format, expected within [SUPPORTED_RANGE]
     * @return the Roman numeral representation of [value]
     */
    fun format(value: Int): String =
        buildString {
            var remainder = value
            for ((amount, symbol) in SYMBOLS) {
                while (remainder >= amount) {
                    append(symbol)
                    remainder -= amount
                }
            }
        }
}
