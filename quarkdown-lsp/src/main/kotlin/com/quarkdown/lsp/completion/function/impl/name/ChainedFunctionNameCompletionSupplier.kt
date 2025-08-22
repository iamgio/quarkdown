package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.function.AbstractFunctionCompletionSupplier
import com.quarkdown.lsp.completion.toCompletionItem
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.findMatchingTokenBeforeIndex
import com.quarkdown.lsp.tokenizer.getTokenAtSourceIndex
import com.quarkdown.lsp.util.remainderUntilIndex
import org.eclipse.lsp4j.CompletionItem
import java.io.File

/**
 * Provides completion items for chained function names in function calls by scanning documentation files.
 *
 * Let `|` be the cursor position in the text, this supplier provides completions for:
 * - `.function::|`
 * - `.function::func|`
 *
 * This supplier is proxied by [FunctionNameCompletionSupplier].
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
class ChainedFunctionNameCompletionSupplier(
    docsDirectory: File,
) : AbstractFunctionCompletionSupplier(docsDirectory) {
    /**
     * Transforms the cursor index to force it to be part of the function call.
     * so that the returned index is always part of the function call.
     *
     * Why:
     * If the cursor is right after the chain separator (`::`), it means
     * the identifier (function name) is missing, hence the separator is parsed as not part of the call.
     *
     * Going back before the separator makes sure the separator is part of the call.
     */
    override fun transformIndex(
        cursorIndex: Int,
        text: String,
    ) = cursorIndex - QuarkdownPatterns.FunctionCall.CHAIN_SEPARATOR.length

    override fun getCompletionItems(
        call: FunctionCall,
        function: DocumentedFunction?,
        cursorIndex: Int,
        originalCursorIndex: Int,
    ): List<CompletionItem> {
        if (!isCompletableCall(call, originalCursorIndex)) {
            return emptyList()
        }

        // The function name snippet to complete.
        val snippet =
            call
                .getTokenAtSourceIndex(originalCursorIndex)
                ?.takeIf { it.type == FunctionCallToken.Type.FUNCTION_NAME }
                ?.lexeme
                ?: ""

        return CacheableFunctionCatalogue
            .searchAll(super.docsDirectory, snippet)
            .map { it.toCompletionItem(chained = true) }
            .toList()
    }

    /**
     * Checks if the cursor is positioned in a function call chain that can be completed.
     * This means the cursor is either immediately after a chain separator or at a function name after a chain separator.
     * @param call the function call at the cursor position
     * @param index the index of the cursor in the source text
     * @return whether the cursor is positioned in a completable chained call
     */
    private fun isCompletableCall(
        call: FunctionCall,
        index: Int,
    ): Boolean {
        // Case 1:
        // the cursor is at the beginning of a chained function call, so the chain separator
        // is not yet part of the call, since the identifier (function name) is missing.
        // For this reason, the separator will be in the remainder.
        if (call.remainderUntilIndex(index) == QuarkdownPatterns.FunctionCall.CHAIN_SEPARATOR) {
            return true
        }

        // Case 2:
        // the chain separator is already part of the call, so we check
        // if the token *before* the cursor is a chain separator.
        call.tokens.findMatchingTokenBeforeIndex(
            index,
            FunctionCallToken.Type.CHAINING_SEPARATOR,
            reset = setOf(FunctionCallToken.Type.FUNCTION_NAME),
        ) ?: return false

        return true
    }
}
