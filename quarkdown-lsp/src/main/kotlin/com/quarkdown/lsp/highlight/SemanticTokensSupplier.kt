package com.quarkdown.lsp.highlight

import org.eclipse.lsp4j.SemanticTokensParams

/**
 * Interface for providing semantic tokens based on the current context in a text document.
 */
interface SemanticTokensSupplier {
    /**
     * Generates a list of semantic tokens.
     * @param params the parameters for the semantic tokens request
     * @param text the current text content of the document
     * @return a list of semantic tokens that can be used for highlighting
     */
    fun getTokens(
        params: SemanticTokensParams,
        text: String,
    ): List<SemanticTokenData>
}
