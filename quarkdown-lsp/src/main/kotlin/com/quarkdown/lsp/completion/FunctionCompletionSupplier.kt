package com.quarkdown.lsp.completion

import com.quarkdown.lsp.documentation.extractContentAsMarkup
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.getChar
import com.quarkdown.quarkdoc.reader.DocsWalker
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
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
            insertText = name
            detail = moduleName
            documentation = Either.forRight(extractor().extractContentAsMarkup())
            kind = CompletionItemKind.Function
        }

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val isFunctionCall = params.position.getChar(text)?.toString() == QuarkdownPatterns.FunctionCall.BEGIN

        if (!isFunctionCall) {
            return emptyList()
        }

        return DokkaHtmlWalker(docsDirectory)
            .walk()
            .filter { it.isInModule }
            .map { it.toCompletionItem() }
            .toList()
    }
}
