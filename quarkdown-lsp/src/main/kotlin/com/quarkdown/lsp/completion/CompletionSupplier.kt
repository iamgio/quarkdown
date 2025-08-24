package com.quarkdown.lsp.completion

import com.quarkdown.lsp.TextDocument
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams

/**
 * Interface for providing completion items based on the current context in a text document.
 *
 * Implementations of this interface should provide logic to generate a list of completion items
 * based on the provided parameters and the current text content.
 */
interface CompletionSupplier {
    /**
     * Generates a list of completion items.
     * @param params the parameters for the completion request, including the position in the document
     * @param document the current document
     * @return a list of completion items that can be suggested
     */
    fun getCompletionItems(
        params: CompletionParams,
        document: TextDocument,
    ): List<CompletionItem>
}
