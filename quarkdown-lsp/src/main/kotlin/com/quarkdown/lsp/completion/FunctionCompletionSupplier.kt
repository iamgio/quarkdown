package com.quarkdown.lsp.completion

import com.quarkdown.lsp.documentation.extractContentAsMarkup
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.sliceFromDelimiterToPosition
import com.quarkdown.quarkdoc.reader.DocsWalker
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * Provider of completion items for function calls.
 * @property docsDirectory the directory containing the documentation files
 */
class FunctionCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
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
    ): List<CompletionItem> {
        // Function name that is being completed.
        val partialName =
            sliceFromDelimiterToPosition(text, params.position, delimiter = QuarkdownPatterns.FunctionCall.BEGIN)
                ?.takeIf { it.all(Char::isLetterOrDigit) }
                ?: return emptyList()

        return DokkaHtmlWalker(docsDirectory)
            .walk()
            .filter { it.isInModule }
            .map { it.toCompletionItem() }
            .filter { it.label.startsWith(partialName, ignoreCase = true) }
            .toList()
    }
}
