package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.ontype.OnTypeFormattingEditSupplier
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.TextEdit

/**
 * Subservice for handling on-type formatting requests.
 * It aggregates edits from all suppliers and returns them as a single list.
 */
class OnTypeFormattingSubservice(
    private val editSuppliers: List<OnTypeFormattingEditSupplier>,
) : TextDocumentSubservice<DocumentOnTypeFormattingParams, List<TextEdit>> {
    override fun process(
        params: DocumentOnTypeFormattingParams,
        document: TextDocument,
    ): List<TextEdit> = editSuppliers.flatMap { it.getEdits(params, document) }
}
