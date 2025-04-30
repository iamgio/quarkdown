package com.quarkdown.core.document.numbering

/**
 * A [NumberingSymbol] that represents a fixed character in a numbering format,
 * such as the dots `.`, in the format `1.A.a`.
 * @param value fixed character to be used
 */
data class NumberingFixedSymbol(val value: Char) : NumberingSymbol
