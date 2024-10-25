package eu.iamgio.quarkdown.document.numbering

import com.github.fracpete.romannumerals4j.RomanNumeralFormat

/**
 * Represents a [NumberingSymbol] within a [NumberingFormat] with the responsibility of
 * counting items (e.g. headings) according to a specific rule (strategy).
 */
interface NumberingCounterSymbol : NumberingSymbol {
    fun map(index: Int): String
}

/**
 * A numbering strategy that counts items as integers: `1, 2, 3, ...`
 */
data object DecimalNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = (1 + index).toString()
}

/**
 * A numbering strategy that counts items as uppercase letters of the latin alphabet: `A, B, C, ...`
 */
data object UppercaseAlphaNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = ('A' + index).toString()
}

/**
 * A numbering strategy that counts items as lowercase letters of the latin alphabet: `a, b, c, ...`
 */
data object LowercaseAlphaNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = ('a' + index).toString()
}

/**
 * A numbering strategy that counts items as uppercase roman numerals: `I, II, III, ...`
 */
data object UppercaseRomanNumberingSymbol : NumberingCounterSymbol {
    // Provided by the romannumerals4j library: https://github.com/fracpete/romannumerals4j
    private val format = RomanNumeralFormat()

    override fun map(index: Int) =
        (1 + index).let {
            format.format(it) ?: throw IllegalStateException("Failed to format $it as a roman numeral")
        }
}

/**
 * A numbering strategy that counts items as lowercase roman numerals: `i, ii, iii, ...`
 */
data object LowecaseRomanNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = UppercaseRomanNumberingSymbol.map(index).lowercase()
}
