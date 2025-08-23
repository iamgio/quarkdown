package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.highlight.SemanticTokenData
import com.quarkdown.lsp.highlight.SemanticTokensEncoder
import com.quarkdown.lsp.highlight.SemanticTokensSupplier
import com.quarkdown.lsp.highlight.toSemanticData
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams

/**
 * Subservice for handling semantic tokens requests.
 * @param tokensSuppliers suppliers of semantic tokens
 */
class SemanticTokensSubservice(
    private val tokensSuppliers: List<SemanticTokensSupplier>,
) : TextDocumentSubservice<SemanticTokensParams, SemanticTokens> {
    override fun process(
        params: SemanticTokensParams,
        document: TextDocument,
    ): SemanticTokens {
        val tokens: List<SemanticTokenData> =
            this.tokensSuppliers
                .flatMap { it.getTokens(params, document) }
                .map { it.toSemanticData(document.text) }

        val encoded = SemanticTokensEncoder.encode(tokens)
        return SemanticTokens(encoded)
    }
}
