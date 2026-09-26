package com.quarkdown.lsp.hover

import com.quarkdown.lsp.documentation.DocsIndexSource
import com.quarkdown.lsp.hover.function.FunctionDocumentationHoverSupplier

/**
 * Factory for creating a list of [HoverSupplier]s.
 */
object HoverSuppliersFactory {
    /**
     * @param docs the source of the documentation index
     * @return the default list of [HoverSupplier] instances
     */
    fun default(docs: DocsIndexSource) =
        listOf(
            FunctionDocumentationHoverSupplier(
                docs = docs,
            ),
        )
}
