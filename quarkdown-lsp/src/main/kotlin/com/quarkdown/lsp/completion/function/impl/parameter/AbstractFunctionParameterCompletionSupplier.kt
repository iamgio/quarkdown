package com.quarkdown.lsp.completion.function.impl.parameter

import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.CompletionSupplier
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
import com.quarkdown.lsp.util.toOffset
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provides completion items for function parameters in function calls by scanning documentation files.
 * This supplier is proxied by [com.quarkdown.lsp.completion.function.FunctionCompletionSupplier].
 * @param docsDirectory the directory containing the documentation files to extract function data from
 * @see FunctionParameterNameCompletionSupplier
 * @see FunctionParameterAllowedValuesCompletionSupplier
 */
internal abstract class AbstractFunctionParameterCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    /**
     * Generates completion items for function parameters based on the provided function data and call context.
     * @param call the parsed function call
     * @param function the documentation data for the function being called
     * @param cursorIndex the index of the cursor in the source text
     * @param originalCursorIndex the original index of the cursor in the source text before any transformations via [transformIndex]
     */
    protected abstract fun getCompletionItems(
        call: FunctionCall,
        function: DocumentedFunction,
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
    ): Int? = cursorIndex

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        // The index of the cursor in the source text.
        val index = params.position.toOffset(text)
        val transformedIndex = transformIndex(index, text) ?: return emptyList()

        val call: FunctionCall =
            FunctionCallTokenizer()
                .getFunctionCalls(text)
                .getAtSourceIndex(transformedIndex)
                ?: return emptyList()

        // Looking up the function data from the documentation to extract available parameters to complete.
        val function: DocumentedFunction = call.getDocumentation(docsDirectory) ?: return emptyList()

        return getCompletionItems(call, function, transformedIndex, index)
    }
}
