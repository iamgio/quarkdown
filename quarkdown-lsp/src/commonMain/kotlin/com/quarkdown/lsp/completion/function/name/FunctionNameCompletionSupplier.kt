package com.quarkdown.lsp.completion.function.name

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.documentation.DocsIndexSource
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CursorPosition

/**
 * Provides completion items for function names in function calls by scanning documentation files.
 * This supplier is proxied by [com.quarkdown.lsp.completion.function.FunctionCompletionSupplier].
 *
 * A name completion can occur:
 * - At the beginning of a function call, e.g. `.xyz`.
 * - In a function call chain, e.g. `.abc::xyz`.
 * @param docs the source of the documentation index to extract function data from
 * @see RegularFunctionNameCompletionSupplier for `.xyz` style completions
 * @see ChainedFunctionNameCompletionSupplier for `.abc::xyz` style completions
 */
class FunctionNameCompletionSupplier(
    private val docs: DocsIndexSource,
) : CompletionSupplier {
    // Completion for function names right after the function begin token ('.').
    private val fromBegin = RegularFunctionNameCompletionSupplier(docs)

    // Completion for function names right after a function call chain token ('::').
    private val fromChain = ChainedFunctionNameCompletionSupplier(docs)

    override fun getCompletionItems(
        position: CursorPosition,
        document: TextDocument,
    ): List<Completion> =
        fromBegin.getCompletionItems(position, document).takeIf { it.isNotEmpty() }
            ?: fromChain.getCompletionItems(position, document)
}
