package com.quarkdown.lsp.completion

import com.quarkdown.lsp.documentation.extractContentAsMarkup
import com.quarkdown.quarkdoc.reader.DocsWalker
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * Provides completion items for function names in function calls by scanning documentation files.
 * This supplier is proxied by [FunctionCompletionSupplier] and expects already-sliced text.
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
internal class FunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    /**
     * Converts a [DocsWalker.Result] to a [CompletionItem] for use in function name completion.
     */
    private fun DocsWalker.Result<*>.toCompletionItem() =
        CompletionItem().apply {
            label = name
            detail = moduleName
            documentation = Either.forRight(extractor().extractContentAsMarkup())
            kind = CompletionItemKind.Function
            insertTextFormat = InsertTextFormat.Snippet
            insertText = FunctionCallSnippet(this@toCompletionItem).getAsString()
        }

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> =
        DokkaHtmlWalker(docsDirectory)
            .walk()
            .filter { it.isInModule }
            .map { it.toCompletionItem() }
            .filter { it.label.startsWith(text, ignoreCase = true) }
            .toList()
}
