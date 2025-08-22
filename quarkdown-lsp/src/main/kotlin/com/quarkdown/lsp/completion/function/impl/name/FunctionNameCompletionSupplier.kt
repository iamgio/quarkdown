package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.completion.function.FunctionCallInsertionSnippet
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import com.quarkdown.lsp.tokenizer.findMatchingTokenBeforeIndex
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
import com.quarkdown.lsp.tokenizer.getTokenAtSourceIndex
import com.quarkdown.lsp.util.getLineUntilPosition
import com.quarkdown.lsp.util.remainderUntilIndex
import com.quarkdown.lsp.util.toOffset
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * Provides completion items for function names in function calls by scanning documentation files.
 * This supplier is proxied by [com.quarkdown.lsp.completion.function.FunctionCompletionSupplier].
 *
 * A name completion can occur:
 * - At the beginning of a function call, e.g. `.xyz`.
 * - In a function call chain, e.g. `.abc::xyz`.
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
internal class FunctionNameCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    // Pattern to match a function call at cursor position.
    private val callPattern = Regex("${QuarkdownPatterns.FunctionCall.identifierInCall}$")

    /**
     * Converts a [DocumentedFunction] to a [CompletionItem] for use in function name completion.
     * @param function the documented function to convert
     * @param chained whether the function is chained call, hence the first parameter should not be included in the snippet
     */
    private fun toCompletionItem(
        function: DocumentedFunction,
        chained: Boolean,
    ) = CompletionItem().apply {
        label = function.name
        detail = function.rawData.moduleName
        documentation = Either.forRight(function.documentationAsMarkup)
        kind = CompletionItemKind.Function
        insertTextFormat = InsertTextFormat.Snippet
        insertText = FunctionCallInsertionSnippet.forFunction(function.data, chained)
    }

    /**
     * @param snippet the function name snippet to match against, such as "al" in "align"
     * @param chained whether the function is a chained call
     * @return completion items for function names that match the given snippet
     */
    private fun getItems(
        snippet: String,
        chained: Boolean,
    ): List<CompletionItem> =
        CacheableFunctionCatalogue
            .getCatalogue(docsDirectory)
            .filter { it.data.name.startsWith(snippet, ignoreCase = true) }
            .map { toCompletionItem(it, chained) }
            .toList()

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> =
        this.fromBegin(params, text).takeIf { it.isNotEmpty() }
            ?: this.fromChain(params, text)

    /**
     * Extracts function name completions from the beginning of a function call: `.function`.
     * This used a lightweight approach to avoid invoking the full tokenizer.
     */
    private fun fromBegin(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val line = params.position.getLineUntilPosition(text) ?: return emptyList()

        // The name of the function call at the cursor position to complete.
        val snippet: String =
            callPattern.find(line)?.value
                ?: return emptyList()

        return getItems(snippet, chained = false)
    }

    /**
     * Extracts function name completions from a function call chain: `.function1::function2`.
     * This makes use of the full tokenizer to figure if the cursor is positioned in a function call chain.
     */
    private fun fromChain(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val index = params.position.toOffset(text)

        // The call at the cursor position.
        val call: FunctionCall =
            FunctionCallTokenizer()
                .getFunctionCalls(text)
                .getAtSourceIndex(index - QuarkdownPatterns.FunctionCall.CHAIN_SEPARATOR.length)
                ?: return emptyList()

        // Making sure the cursor is completing a chained function call name.
        if (!isCompletableChainedCall(call, index)) {
            return emptyList()
        }

        // The function name snippet to complete.
        val snippet =
            call
                .getTokenAtSourceIndex(index)
                ?.takeIf { it.type == FunctionCallToken.Type.FUNCTION_NAME }
                ?.lexeme
                ?: ""

        return getItems(snippet, chained = true)
    }

    /**
     * Checks if the cursor is positioned in a function call chain that can be completed.
     * This means the cursor is either immediately after a chain separator or at a function name after a chain separator.
     * @param call the function call at the cursor position
     * @param index the index of the cursor in the source text
     * @return whether the cursor is positioned in a completable chained call
     */
    private fun isCompletableChainedCall(
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
