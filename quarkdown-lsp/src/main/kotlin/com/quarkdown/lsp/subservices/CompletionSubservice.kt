package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.completion.CompletionSupplier
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams

/**
 * Subservice for handling completion requests.
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
            .flatMap { it.getCompletionItems(params, document) }
}
