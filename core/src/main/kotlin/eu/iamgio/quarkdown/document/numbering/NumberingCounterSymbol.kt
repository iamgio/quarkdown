package eu.iamgio.quarkdown.document.numbering

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
class DecimalNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = (1 + index).toString()
}

/**
 * A numbering strategy that counts items as lowercase letters of the latin alphabet: `a, b, c, ...`
 */
class LowercaseAlphaNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = ('a' + index).toString()
}

/**
 * A numbering strategy that counts items as uppercase letters of the latin alphabet: `A, B, C, ...`
 */
class UppercaseAlphaNumberingSymbol : NumberingCounterSymbol {
    override fun map(index: Int) = ('A' + index).toString()
}
