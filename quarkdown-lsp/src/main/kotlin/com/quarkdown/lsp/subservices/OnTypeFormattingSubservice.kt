package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.TextPatch
import com.quarkdown.lsp.ontype.OnTypeFormattingEditSupplier

/**
 * Subservice for handling on-type formatting requests.
 * It aggregates edits from all suppliers and returns them as a single list.
 */
class OnTypeFormattingSubservice(
    private val editSuppliers: List<OnTypeFormattingEditSupplier>,
) : TextDocumentSubservice<CursorPosition, List<TextPatch>> {
    override fun process(
        params: CursorPosition,
        document: TextDocument,
    ): List<TextPatch> = editSuppliers.flatMap { it.getEdits(params, document) }
}
