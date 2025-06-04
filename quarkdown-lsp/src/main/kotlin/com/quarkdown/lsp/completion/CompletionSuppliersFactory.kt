package com.quarkdown.lsp.completion

import com.quarkdown.lsp.QuarkdownLanguageServer

/**
 * Factory for creating a list of [CompletionSupplier]s.
 * @property server the Quarkdown language server instance
 */
class CompletionSuppliersFactory(
    private val server: QuarkdownLanguageServer,
) {
    /**
     * @return the default list of [CompletionSupplier] instances
     */
    fun default() =
        listOf(
            FunctionCompletionSupplier(docsDirectory = server.docsDirectory ?: error("Docs directory not available")),
        )
}
