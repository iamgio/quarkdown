package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.highlight.SemanticTokenData
import com.quarkdown.lsp.highlight.SemanticTokensEncoder
import com.quarkdown.lsp.highlight.SemanticTokensSupplier
import com.quarkdown.lsp.highlight.toSemanticData

/**
 * Subservice for handling semantic tokens requests.
 * @param tokensSuppliers suppliers of semantic tokens
 */
class SemanticTokensSubservice(
    private val tokensSuppliers: List<SemanticTokensSupplier>,
) : TextDocumentSubservice<Unit, List<Int>> {
    /**
     * @return the semantic tokens of [document], encoded as by the LSP specification
     */
    override fun process(
        params: Unit,
        document: TextDocument,
    ): List<Int> {
        val tokens: List<SemanticTokenData> =
            this.tokensSuppliers
                .flatMap { it.getTokens(document) }
                .map { it.toSemanticData(document.text) }

        return SemanticTokensEncoder.encode(tokens)
    }
}
