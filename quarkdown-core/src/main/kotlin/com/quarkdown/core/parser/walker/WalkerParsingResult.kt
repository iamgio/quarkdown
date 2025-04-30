package com.quarkdown.core.parser.walker

/**
 * The result of a [WalkerParser] parsing operation.
 * @param T the type of result, produced by the parser
 * @param value the result value, produced by the parser
 * @param endIndex the index, relative to the input string, at which the parsing operation ended
 * @param remainder the remaining content of the input string after the parsing operation
 * @see WalkerParser
 */
data class WalkerParsingResult<T>(val value: T, val endIndex: Int, val remainder: CharSequence)
