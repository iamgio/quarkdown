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

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        val index = params.position.toOffset(text)
        val call: FunctionCall =
            FunctionCallTokenizer()
                .getFunctionCalls(text)
                .getAtSourceIndex(index)
                ?: return emptyList()

        // The parsed function call associated with the token.
        val result = call.parserResult
        val parsedCall = result.value

        // Looking up the function data from the documentation to extract available parameters to complete.
        val function: DocsFunction = getFunctionData(parsedCall.name) ?: return emptyList()

        return getCompletionItems(call, function, index)
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
