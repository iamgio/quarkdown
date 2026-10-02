package com.quarkdown.core.parser.walker

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence

/**
 * The result of a [WalkerParser] parsing operation.
 * @param T the type of result, produced by the parser
 * @param value the result value, produced by the parser
 * @param endIndex the index, relative to the input string, at which the parsing operation ended
 * @param tokens the sequence of tokens that were matched during the tokenization by the walker
 * @param source the whole input the walker was given
 * @see WalkerParser
 */
data class WalkerParsingResult<T>(
    val value: T,
    val endIndex: Int,
    val tokens: TokenMatchesSequence,
    val source: CharSequence,
) {
    /**
     * The part of [source] that was parsed
     */
    val sourceText: CharSequence
        get() = source.subSequence(0, endIndex)

    /**
     * The remaining content of [source] after the parsing operation.
     */
    val remainder: CharSequence
        get() = source.subSequence(endIndex, source.length)
}
