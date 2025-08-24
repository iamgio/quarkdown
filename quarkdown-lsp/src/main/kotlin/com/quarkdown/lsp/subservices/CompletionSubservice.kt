package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.completion.CompletionSupplier
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams

/**
 * Subservice for handling completion requests.
 * Only the first non-empty result from the suppliers is returned.
 * @param completionSuppliers suppliers of completion items
 */
class CompletionSubservice(
    private val completionSuppliers: List<CompletionSupplier>,
) : TextDocumentSubservice<CompletionParams, List<CompletionItem>> {
    override fun process(
        params: CompletionParams,
        document: TextDocument,
    ): List<CompletionItem> =
        completionSuppliers
            .asSequence()
            .map { it.getCompletionItems(params, document) }
            .firstOrNull { it.isNotEmpty() }
            ?: emptyList()
}
