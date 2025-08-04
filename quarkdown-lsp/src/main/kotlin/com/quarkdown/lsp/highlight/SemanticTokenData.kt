package com.quarkdown.lsp.highlight

/**
 * A single semantic token for use in semantic highlighting.
 * @param line zero-based line number of the token
 * @param startChar zero-based character offset at which the token starts
 * @param length length of the token in characters
 * @param tokenType encoded token type index
 * @param tokenModifiers encoded bitmask of token modifiers
 */
data class SemanticTokenData(
    val line: Int,
    val startChar: Int,
    val length: Int,
    val tokenType: Int,
    val tokenModifiers: Int,
)
