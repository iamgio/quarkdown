package com.quarkdown.lsp.completion.function.name

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.completion.toCompletionItem
import com.quarkdown.lsp.documentation.DocsIndexSource
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.getLineUntilPosition

/**
 * Provides completion items for regular (non-chained) function names by scanning documentation files.
 *
 * Let `|` be the cursor position in the text, this supplier provides completions for:
 * - `.|`
 * - `.func|`
 *
 * This supplier is proxied by [FunctionNameCompletionSupplier].
 * @param docs the source of the documentation index to extract function data from
 */
class RegularFunctionNameCompletionSupplier(
    private val docs: DocsIndexSource,
) : CompletionSupplier {
    // Pattern to match a function call at cursor position.
    private val callPattern = Regex("${QuarkdownPatterns.FunctionCall.identifierInCall}$")

    override fun getCompletionItems(
        position: CursorPosition,
        document: TextDocument,
    ): List<Completion> {
        val text = document.text
        val line = position.getLineUntilPosition(text) ?: return emptyList()

        // The name of the function call at the cursor position to complete.
        val snippet: String = callPattern.find(line)?.value ?: return emptyList()

        return CacheableFunctionCatalogue
            .searchAll(this.docs, snippet)
            .map { it.toCompletionItem(chained = false) }
            .toList()
    }
}
