package com.quarkdown.lsp.highlight

/**
 * Encodes a list of [SemanticTokenData] into the LSP semantic tokens format.
 */
object SemanticTokensEncoder {
    /**
     * Encodes a list of semantic tokens into a flat list of integers according to the LSP semantic tokens format.
     * The encoding format is as follows:
     * - delta line number
     * - delta start character
     * - length of the token
     * - token type
     * - token modifiers
     *
     * @param tokens the list of semantic tokens to encode
     * @return a flat list of integers representing the encoded tokens
     */
    fun encode(tokens: List<SemanticTokenData>): List<Int> =
        buildList {
            var lastLine = 0
            var lastChar = 0

            val sortedTokens = tokens.asSequence().sortedWith(compareBy({ it.line }, { it.startChar }))

            sortedTokens.forEach { (line, startChar, length, tokenType, tokenModifiers) ->
                val deltaLine = line - lastLine
                val deltaStart = if (deltaLine == 0) startChar - lastChar else startChar

                lastLine = line
                lastChar = startChar

                add(deltaLine)
                add(deltaStart)
                add(length)
                add(tokenType)
                add(tokenModifiers)
            }
        }
}
