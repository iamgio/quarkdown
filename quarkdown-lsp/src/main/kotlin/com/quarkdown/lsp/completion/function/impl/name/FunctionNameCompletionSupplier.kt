package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.completion.function.FunctionCallInsertionSnippet
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * Provides completion items for function names in function calls by scanning documentation files.
 * This supplier is proxied by [com.quarkdown.lsp.completion.function.FunctionCompletionSupplier] and expects already-sliced text.
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
internal class FunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
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
    ): List<CompletionItem> =
        CacheableFunctionCatalogue
            .getCatalogue(docsDirectory)
            .filter { it.data.name.startsWith(text, ignoreCase = true) }
            .map(::toCompletionItem)
            .toList()
}
