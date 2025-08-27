package com.quarkdown.lsp.completion

import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.function.FunctionCallInsertionSnippet
import com.quarkdown.lsp.documentation.htmlToMarkup
import com.quarkdown.quarkdoc.reader.DocsParameter
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either

// Converters from various types to LSP completion items.

/**
 * Converts a [DocumentedFunction] to a [CompletionItem] for use in function name completion.
 * @param function the documented function to convert
 * @param chained whether the function is chained call, hence the first parameter should not be included in the snippet
 */
fun DocumentedFunction.toCompletionItem(chained: Boolean) =
    CompletionItem().apply {
        label = name
        detail = rawData.moduleName
        documentation = Either.forRight(documentationAsMarkup)
        kind = CompletionItemKind.Function
        insertTextFormat = InsertTextFormat.Snippet
        insertText = FunctionCallInsertionSnippet.forFunction(this@toCompletionItem.data, chained)
    }

/**
 * Converts a [DocsParameter] to a [CompletionItem] for use in parameter name completion.
 */
fun DocsParameter.toCompletionItem() =
    CompletionItem().apply {
        label = name
        detail = if (!isOptional) "required" else null
        documentation = Either.forRight(description.htmlToMarkup())
        kind = CompletionItemKind.Field
        insertTextFormat = InsertTextFormat.Snippet
        insertText = FunctionCallInsertionSnippet.forParameter(this@toCompletionItem, alwaysNamed = true)
    }

/**
 * Converts a generic string value, such as an allowed value for a parameter, to a [CompletionItem].
 */
fun String.toCompletionItem() =
    CompletionItem().apply {
        label = this@toCompletionItem
        kind = CompletionItemKind.Value
        insertTextFormat = InsertTextFormat.Snippet
    }
