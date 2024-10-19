package eu.iamgio.quarkdown.document.numbering

/**
 * A [NumberingSymbol] that represents a fixed character in a numbering format.
 * For example the dots `.`, in the format `1.A.a`:
 */
data class NumberingFixedSymbol(val symbol: Char) : NumberingSymbol
