package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.completion.CompletionSupplier
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provides completion items for function names in function calls by scanning documentation files.
 * This supplier is proxied by [com.quarkdown.lsp.completion.function.FunctionCompletionSupplier].
 *
 * A name completion can occur:
 * - At the beginning of a function call, e.g. `.xyz`.
 * - In a function call chain, e.g. `.abc::xyz`.
 * @param docsDirectory the directory containing the documentation files to extract function data from
 * @see RegularFunctionNameCompletionSupplier for `.xyz` style completions
 * @see ChainedFunctionNameCompletionSupplier for `.abc::xyz` style completions
 */
internal class FunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    // Completion for function names right after the function begin token ('.').
    private val fromBegin = RegularFunctionNameCompletionSupplier(docsDirectory)

    // Completion for function names right after a function call chain token ('::').
    private val fromChain = ChainedFunctionNameCompletionSupplier(docsDirectory)

    override fun getCompletionItems(
        params: CompletionParams,
        document: TextDocument,
    ): List<CompletionItem> =
        fromBegin.getCompletionItems(params, document).takeIf { it.isNotEmpty() }
            ?: fromChain.getCompletionItems(params, document)
}
