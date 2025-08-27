package com.quarkdown.lsp.highlight

import com.quarkdown.lsp.util.offsetToPosition

/**
 * A simplified version of a semantic token, used for initial processing.
 * This can be converted to a full semantic token via [toSemanticData].
 * @param range the character range, inclusive, of the token in the text
 * @param type the type of the token, which is an index into the semantic token legend
 */
data class SimpleTokenData(
    val range: IntRange,
    val type: TokenType,
)

/**
 * A semantic token for use in semantic highlighting.
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

/**
 * Converts a [SimpleTokenData] to a full [SemanticTokenData] which adheres to the LSP specification.
 * @param text the text content of the document, used to calculate the position
 * @return a full semantic token data
 */
fun SimpleTokenData.toSemanticData(text: String): SemanticTokenData {
    val start = range.first
    val end = range.endInclusive

    val pos = offsetToPosition(text, start)
    val length = end - start

    return SemanticTokenData(pos.line, pos.character, length, type.index, 0)
}
