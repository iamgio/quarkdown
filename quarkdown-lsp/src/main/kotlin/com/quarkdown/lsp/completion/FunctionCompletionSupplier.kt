package com.quarkdown.lsp.completion

import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.sliceFromDelimiterToPosition
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provider of completion items for function calls,
 * which acts as a proxy for [FunctionNameCompletionSupplier] and [FunctionParameterCompletionSupplier]
 * depending on the context of the completion request.
 * @property docsDirectory the directory containing the documentation files
 */
class FunctionCompletionSupplier(
    docsDirectory: File,
) : CompletionSupplier {
    // Completion for function names.
    private val nameCompletionSupplier =
        FunctionNameCompletionSupplier(docsDirectory)

    // Completion for function parameters.
    private val parameterCompletionSupplier =
        FunctionParameterCompletionSupplier(docsDirectory)

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val begin = QuarkdownPatterns.FunctionCall.BEGIN

        // Function snippet that is being completed.
        val snippet: String =
            sliceFromDelimiterToPosition(text, params.position, delimiter = begin)
                ?: return emptyList()

        return when {
            // The function name is being completed.
            snippet.all { it.isLetterOrDigit() } ->
                nameCompletionSupplier.getCompletionItems(params, snippet)

            // A function parameter is maybe being completed.
            // This is determined through tokenization of the complete function call.
            else ->
                parameterCompletionSupplier.getCompletionItems(params, begin + snippet)
        }
    }
}
