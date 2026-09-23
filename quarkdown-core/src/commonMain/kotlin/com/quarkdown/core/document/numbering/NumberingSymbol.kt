package com.quarkdown.core.document.numbering

/**
 * Any symbol that appears in a numbering format.
 * For example, in the format `1.A.a`:
 * - `1` is a [DecimalNumberingSymbol], which counts `1, 2, 3, ...`
 * - `.` is a [NumberingFixedSymbol]
 * - `A` is an uppercase [AlphaNumberingSymbol], which counts `A, B, C, ...`
 * - `.` is a [NumberingFixedSymbol]
 * - `a` is a lowercase [AlphaNumberingSymbol], which counts `a, b, c, ...`
 * @see NumberingCounterSymbol
 * @see NumberingFixedSymbol
 * @see NumberingFormat
 */
sealed interface NumberingSymbol
