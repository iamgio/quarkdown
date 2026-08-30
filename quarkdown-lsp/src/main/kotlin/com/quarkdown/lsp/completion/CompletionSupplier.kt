package com.quarkdown.lsp.completion

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CursorPosition

/**
 * Interface for providing completion items based on the current context in a text document.
 *
 * Implementations of this interface should provide logic to generate a list of completion items
 * based on the provided parameters and the current text content.
 */
interface CompletionSupplier {
    /**
     * Generates a list of completion items.
     * @param position the position in the document the completion was requested at
     * @param document the current document
     * @return a list of completion items that can be suggested
     */
    fun getCompletionItems(
        position: CursorPosition,
        document: TextDocument,
    ): List<Completion>
}
