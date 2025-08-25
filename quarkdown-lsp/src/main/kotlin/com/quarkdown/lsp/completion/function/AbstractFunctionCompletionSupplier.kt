package com.quarkdown.lsp.completion.function

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
import com.quarkdown.lsp.util.toOffset
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provides completion items for function calls by scanning documentation files.
 * @param docsDirectory the directory containing the documentation files to extract function data from
 * @see com.quarkdown.lsp.completion.function.impl.parameter
 * @see com.quarkdown.lsp.completion.function.impl.name
 */
abstract class AbstractFunctionCompletionSupplier(
    protected val docsDirectory: File,
) : CompletionSupplier {
    /**
     * Generates completion items based on the provided function data and call context.
     * @param call the parsed function call
     * @param function the documentation data for the function being called
     * @param cursorIndex the index of the cursor in the source text
     * @param originalCursorIndex the original index of the cursor in the source text before any transformations via [transformIndex]
     */
    protected abstract fun getCompletionItems(
        call: FunctionCall,
        function: DocumentedFunction?,
        cursorIndex: Int,
        originalCursorIndex: Int,
    ): List<CompletionItem>

    /**
     * Transforms the cursor index to a suitable index for processing.
     * This can be overridden to adjust how the cursor index is interpreted.
     * @param cursorIndex the original cursor index in the text
     * @param text the text being processed
     * @return the transformed cursor index, or null if no valid index can be determined
     */
    protected open fun transformIndex(
        cursorIndex: Int,
        text: String,
    ): Int = cursorIndex

    override fun getCompletionItems(
        params: CompletionParams,
        document: TextDocument,
    ): List<CompletionItem> {
        val text = document.text

        // The index of the cursor in the source text.
        val index = params.position.toOffset(text)
        val transformedIndex = transformIndex(index, text).takeIf { it >= 0 } ?: return emptyList()

        val call: FunctionCall =
            document.functionCalls
                .getAtSourceIndex(transformedIndex)
                ?: return emptyList()

        // Looking up the function data from the documentation to extract available parameters to complete.
        val function: DocumentedFunction? = call.getDocumentation(docsDirectory)

        return getCompletionItems(call, function, transformedIndex, index)
    }
}
