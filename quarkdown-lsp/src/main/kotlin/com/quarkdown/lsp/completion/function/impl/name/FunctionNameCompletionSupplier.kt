package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.completion.function.FunctionCallInsertionSnippet
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.getLineUntilPosition
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * Provides completion items for function names in function calls by scanning documentation files.
 * This supplier is proxied by [com.quarkdown.lsp.completion.function.FunctionCompletionSupplier].
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
internal class FunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    // Pattern to match a function call at cursor position.
    private val callPattern = Regex("${QuarkdownPatterns.FunctionCall.identifierInCall}$")

    /**
     * Converts a [DocumentedFunction] to a [CompletionItem] for use in function name completion.
     */
    private fun toCompletionItem(function: DocumentedFunction) =
        CompletionItem().apply {
            label = function.name
            detail = function.rawData.moduleName
            documentation = Either.forRight(function.documentationAsMarkup)
            kind = CompletionItemKind.Function
            insertTextFormat = InsertTextFormat.Snippet
            insertText = FunctionCallInsertionSnippet.forFunction(function.data)
        }

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        // Function snippet that is being completed, obtained by backtracking from the cursor position.
        // This is a lightweight approach to avoid invoking the full tokenizer,
        // but cannot be relied on for all cases, such as nested function calls.
        val line = params.position.getLineUntilPosition(text) ?: return emptyList()
        val snippet: String =
            callPattern
                .find(line)
                ?.value
                ?: return emptyList()

        return getItems(snippet)
    }

    private fun getItems(snippet: String): List<CompletionItem> =
        CacheableFunctionCatalogue
            .getCatalogue(docsDirectory)
            .filter { it.data.name.startsWith(snippet, ignoreCase = true) }
            .map(::toCompletionItem)
            .toList()
}
