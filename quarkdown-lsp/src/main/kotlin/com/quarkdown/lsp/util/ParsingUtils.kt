package com.quarkdown.lsp.util

import com.quarkdown.core.util.substringWithinBounds
import com.quarkdown.lsp.tokenizer.FunctionCall

/**
 * Returns the remainder of the parsing result, truncated to the specified index relative to the original source text.
 *
 * For example, if the source is `"Hello, world!"` and the parsing completes with `"Hello"`,
 * the regular remainder would be `", world!"`.
 * If this function is called with an index of `10`, it will return `", wor"`.
 * @param index the index to which the remainder should be returned
 * @return the substring from the start of the remainder to the specified index
 */
fun FunctionCall.remainderUntilIndex(index: Int): String? =
    parserResult.remainder
        .substringWithinBounds(0, index - range.endInclusive)
