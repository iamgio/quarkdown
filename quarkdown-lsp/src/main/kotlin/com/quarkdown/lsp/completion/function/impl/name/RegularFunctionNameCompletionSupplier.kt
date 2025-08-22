package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.completion.toCompletionItem
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.getLineUntilPosition
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provides completion items for regular (non-chained) function names by scanning documentation files.
 *
 * Let `|` be the cursor position in the text, this supplier provides completions for:
 * - `.|`
 * - `.func|`
 *
 * This supplier is proxied by [FunctionNameCompletionSupplier].
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
class RegularFunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    // Pattern to match a function call at cursor position.
    private val callPattern = Regex("${QuarkdownPatterns.FunctionCall.identifierInCall}$")

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val line = params.position.getLineUntilPosition(text) ?: return emptyList()

        // The name of the function call at the cursor position to complete.
        val snippet: String = callPattern.find(line)?.value ?: return emptyList()

        return CacheableFunctionCatalogue
            .searchAll(this.docsDirectory, snippet)
            .map { it.toCompletionItem(chained = false) }
            .toList()
    }
}
