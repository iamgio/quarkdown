package com.quarkdown.lsp.completion

import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
import com.quarkdown.lsp.util.toOffset
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provides completion items for function parameters in function calls by scanning documentation files.
 * This supplier is proxied by [FunctionCompletionSupplier].
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
     */
    protected abstract fun getCompletionItems(
        call: FunctionCall,
        function: DocsFunction,
        cursorIndex: Int,
    ): List<CompletionItem>

    /**
     * Transforms the cursor index to a suitable index for processing.
     * This can be overridden to adjust how the cursor index is interpreted.
     * @param cursorIndex the original cursor index in the text
     * @param text the text being processed (before [transformText])
     * @return the transformed cursor index, or null if no valid index can be determined
     */
    protected open fun transformIndex(
        cursorIndex: Int,
        text: String,
    ): Int? = cursorIndex

    /**
     * Transforms the text to a suitable format for processing.
     * This can be overridden to adjust how the text is interpreted.
     * @param cursorIndex the index of the cursor in the text (after [transformIndex])
     * @param text the original text being processed
     * @return the transformed text
     */
    protected open fun transformText(
        cursorIndex: Int,
        text: String,
    ): String = text

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val index = params.position.toOffset(text)

        // The text that contains the function call.
        val transformedText = transformText(index, text)
        // The index of the cursor in the source text.
        val transformedIndex =
            transformIndex(index, transformedText) ?: return emptyList()

        val call: FunctionCall =
            FunctionCallTokenizer()
                .getFunctionCalls(transformedText)
                .getAtSourceIndex(transformedIndex)
                ?: return emptyList()

        // The parsed function call associated with the token.
        val result = call.parserResult
        val parsedCall = result.value

        // Looking up the function data from the documentation to extract available parameters to complete.
        val function: DocsFunction = getFunctionData(parsedCall.name) ?: return emptyList()

        return getCompletionItems(call, function, transformedIndex)
    }

    /**
     * Retrieves the function data for the given function name from the documentation.
     * @param functionName the name of the function to retrieve data for
     * @return the [DocsFunction] if found
     */
    private fun getFunctionData(functionName: String): DocsFunction? =
        DokkaHtmlWalker(docsDirectory)
            .walk()
            .filter { it.isInModule }
            .find { it.name == functionName }
            ?.extractor()
            ?.extractFunctionData()
}
