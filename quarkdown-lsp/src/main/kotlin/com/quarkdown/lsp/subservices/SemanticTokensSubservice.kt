package com.quarkdown.lsp.subservices

import com.quarkdown.core.util.normalizeLineSeparators
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
        text: String,
    ): SemanticTokens {
        // Lexers replace line endings with LF, so it's normalized here too to ensure consistency.
        val normalizedText = text.normalizeLineSeparators().toString()

        val tokens: List<SemanticTokenData> =
            this.tokensSuppliers
                .flatMap { it.getTokens(params, normalizedText) }
                .map { it.toSemanticData(normalizedText) }

        val encoded = SemanticTokensEncoder.encode(tokens)
        return SemanticTokens(encoded)
    }
}
