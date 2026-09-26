package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CursorPosition

/**
 * Subservice for handling completion requests.
 * Only the first non-empty result from the suppliers is returned.
 * @param completionSuppliers suppliers of completion items
 */
class CompletionSubservice(
    private val completionSuppliers: List<CompletionSupplier>,
) : TextDocumentSubservice<CursorPosition, List<Completion>> {
    override fun process(
        params: CursorPosition,
        document: TextDocument,
    ): List<Completion> =
        completionSuppliers
            .asSequence()
            .map { it.getCompletionItems(params, document) }
            .firstOrNull { it.isNotEmpty() }
            ?: emptyList()
}
