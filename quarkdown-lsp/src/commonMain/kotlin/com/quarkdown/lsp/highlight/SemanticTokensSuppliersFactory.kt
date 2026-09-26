package com.quarkdown.lsp.highlight

import com.quarkdown.lsp.highlight.function.FunctionCallTokensSupplier

/**
 * Factory for creating a list of [SemanticTokensSupplier]s.
 */
object SemanticTokensSuppliersFactory {
    /**
     * @return the default list of [SemanticTokensSupplier] instances
     */
    fun default() =
        listOf(
            FunctionCallTokensSupplier(),
        )
}
