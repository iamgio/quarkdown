package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.hover.HoverSupplier
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams

/**
 * Subservice for handling hover requests.
 * It gathers hover information from multiple suppliers, and picks the first non-null result.
 * @param hoverSuppliers suppliers of hover information
 */
class HoverSubservice(
    private val hoverSuppliers: List<HoverSupplier>,
) : TextDocumentSubservice<HoverParams, Hover?> {
    override fun process(
        params: HoverParams,
        document: TextDocument,
    ): Hover? =
        hoverSuppliers
            .asSequence()
            .mapNotNull { it.getHover(params, document) }
            .firstOrNull()
}
