package com.quarkdown.core.parser.walker

import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence

/**
 * The result of a [WalkerParser] parsing operation.
 * @param T the type of result, produced by the parser
 * @param value the result value, produced by the parser
 * @param endIndex the index, relative to the input string, at which the parsing operation ended
 * @param tokens the sequence of tokens that were matched during the tokenization by the walker
 * @param sourceText the original input string that was parsed
 * @param remainder the remaining content of the input string after the parsing operation
 * @see WalkerParser
 */
data class WalkerParsingResult<T>(
    val value: T,
    val endIndex: Int,
    val tokens: TokenMatchesSequence,
    val sourceText: CharSequence,
    val remainder: CharSequence,
)
