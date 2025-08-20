package com.quarkdown.lsp.hover

import com.quarkdown.lsp.QuarkdownLanguageServer
import com.quarkdown.lsp.hover.function.FunctionDocumentationHoverSupplier

/**
 * Factory for creating a list of [HoverSupplier]s.
 * @param server the Quarkdown language server instance
 */
class HoverSuppliersFactory(
    private val server: QuarkdownLanguageServer,
) {
    /**
     * @return the default list of [HoverSupplier] instances
     */
    fun default() =
        listOf(
            FunctionDocumentationHoverSupplier(
                docsDirectory = server.docsDirectory ?: error("Docs directory not available"),
            ),
        )
}
