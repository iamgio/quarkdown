package com.quarkdown.lsp.completion.function.impl.name

import com.quarkdown.core.util.substringWithinBounds
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
import com.quarkdown.lsp.util.toOffset
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * The lexer does not recognize function calls without a function name,
 * hence, to provide completions, this mock identifier is appended to
 * trick the lexer into recognizing a function call.
 */
private const val MOCK_IDENFIFIER_SUFFIX = "a"

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
     */
    private fun toCompletionItem(function: DocumentedFunction) =
        CompletionItem().apply {
            label = function.name
            detail = function.rawData.moduleName
            documentation = Either.forRight(function.documentationAsMarkup)
            kind = CompletionItemKind.Function
            insertTextFormat = InsertTextFormat.Snippet
            insertText = FunctionCallInsertionSnippet.forFunction(function.data)
        }

    /**
     * @param snippet the function name snippet to match against, such as "al" in "align"
     * @return completion items for function names that match the given snippet
     */
    private fun getItems(snippet: String): List<CompletionItem> =
        CacheableFunctionCatalogue
            .getCatalogue(docsDirectory)
            .filter { it.data.name.startsWith(snippet, ignoreCase = true) }
            .map(::toCompletionItem)
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

        return getItems(snippet)
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

        // The text until the cursor position, with the mock identifier appended.
        val mockText = text.substringWithinBounds(0, index) + MOCK_IDENFIFIER_SUFFIX

        // The call at the cursor position.
        val call: FunctionCall? = FunctionCallTokenizer().getFunctionCalls(mockText).getAtSourceIndex(index)

        // Making sure the cursor is completing a chained function call name.
        call?.tokens?.findMatchingTokenBeforeIndex(
            index,
            FunctionCallToken.Type.CHAINING_SEPARATOR,
            reset = setOf(FunctionCallToken.Type.FUNCTION_NAME),
        ) ?: return emptyList()

        // The function name snippet to complete.
        val snippet =
            call
                .getTokenAtSourceIndex(index + MOCK_IDENFIFIER_SUFFIX.length)
                ?.takeIf { it.type == FunctionCallToken.Type.FUNCTION_NAME }
                ?.lexeme
                ?.removeSuffix(MOCK_IDENFIFIER_SUFFIX)
                ?: return emptyList()

        return getItems(snippet)
    }
}
