package com.quarkdown.lsp.completion.function.name

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.model.Completion
import com.quarkdown.lsp.model.CursorPosition
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
class FunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    // Completion for function names right after the function begin token ('.').
    private val fromBegin = RegularFunctionNameCompletionSupplier(docsDirectory)

    // Completion for function names right after a function call chain token ('::').
    private val fromChain = ChainedFunctionNameCompletionSupplier(docsDirectory)

    override fun getCompletionItems(
        position: CursorPosition,
        document: TextDocument,
    ): List<Completion> =
        fromBegin.getCompletionItems(position, document).takeIf { it.isNotEmpty() }
            ?: fromChain.getCompletionItems(position, document)
}
