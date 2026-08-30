package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.hover.HoverSupplier
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.HoverInfo

/**
 * Subservice for handling hover requests.
 * It gathers hover information from multiple suppliers, and picks the first non-null result.
 * @param hoverSuppliers suppliers of hover information
 */
class HoverSubservice(
    private val hoverSuppliers: List<HoverSupplier>,
) : TextDocumentSubservice<CursorPosition, HoverInfo?> {
    override fun process(
        params: CursorPosition,
        document: TextDocument,
    ): HoverInfo? =
        hoverSuppliers
            .asSequence()
            .mapNotNull { it.getHover(params, document) }
            .firstOrNull()
}
