package com.quarkdown.lsp.util

import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.util.substringWithinBounds

/**
 * Returns the remainder of the parsing result, truncated to the specified index relative to the original source text.
 *
 * For example, if the source is `"Hello, world!"` and the parsing completes with `"Hello"`,
 * the regular remainder would be `", world!"`.
 * If this function is called with an index of `10`, it will return `", wor"`.
 * @param index The index to which the remainder should be returned.
 * @return The substring from the start of the remainder to the specified index.
 */
fun WalkerParsingResult<*>.remainderUntilIndex(index: Int): String = remainder.substringWithinBounds(0, endIndex - index)
