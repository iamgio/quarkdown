package com.quarkdown.lsp.highlight

import com.quarkdown.lsp.QuarkdownLanguageServer

/**
 * Factory for creating a list of [SemanticTokensSupplier]s.
 * @property server the Quarkdown language server instance
 */
class SemanticTokensSuppliersFactory(
    private val server: QuarkdownLanguageServer,
) {
    /**
     * @return the default list of [SemanticTokensSupplier] instances
     */
    fun default() =
        listOf(
            FunctionCallTokensSupplier(),
        )
}
