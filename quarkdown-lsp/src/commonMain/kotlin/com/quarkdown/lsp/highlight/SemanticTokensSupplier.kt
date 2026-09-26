package com.quarkdown.lsp.highlight

import com.quarkdown.lsp.TextDocument

/**
 * Interface for providing semantic tokens based on the current context in a text document.
 */
interface SemanticTokensSupplier {
    /**
     * Generates a list of simplified semantic tokens, which will be converted to full semantic tokens later.
     * @param document the current document
     * @return a list of semantic tokens that can be used for highlighting
     */
    fun getTokens(document: TextDocument): Iterable<SimpleTokenData>
}
