package com.quarkdown.lsp.hover

import com.quarkdown.lsp.QuarkdownLanguageServer
import com.quarkdown.lsp.hover.function.FunctionDocumentationHoverSupplier

/**
 * Factory for creating a list of [HoverSupplier]s.
 */
object HoverSuppliersFactory {
    /**
     * @param server the Quarkdown language server instance
     * @return the default list of [HoverSupplier] instances
     */
    fun default(server: QuarkdownLanguageServer) =
        listOf(
            FunctionDocumentationHoverSupplier(
                docsDirectory = server.docsDirectoryOrThrow(),
            ),
        )
}
