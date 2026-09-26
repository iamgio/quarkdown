package com.quarkdown.lsp.completion

import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.function.FunctionCallInsertionSnippet
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CompletionKind
import com.quarkdown.quarkdoc.reader.DocsParameter

// Converters from various types to completion proposals.

/**
 * Converts a [DocumentedFunction] to a [Completion] for use in function name completion.
 * @param chained whether the function is a chained call, hence the first parameter should not be included in the snippet
 */
fun DocumentedFunction.toCompletionItem(chained: Boolean) =
    Completion(
        label = name,
        kind = CompletionKind.FUNCTION,
        detail = rawData.moduleName,
        documentationMarkdown = documentationMarkdown,
        insertionSnippet = FunctionCallInsertionSnippet.forFunction(data, chained),
    )

/**
 * Converts a [DocsParameter] to a [Completion] for use in parameter name completion.
 */
fun DocsParameter.toCompletionItem() =
    Completion(
        label = name,
        kind = CompletionKind.PARAMETER,
        detail = if (!isOptional) "required" else null,
        documentationMarkdown = description,
        insertionSnippet = FunctionCallInsertionSnippet.forParameter(this, alwaysNamed = true),
    )

/**
 * Converts a generic string value, such as an allowed value for a parameter, to a [Completion].
 */
fun String.toCompletionItem() =
    Completion(
        label = this,
        kind = CompletionKind.VALUE,
    )
