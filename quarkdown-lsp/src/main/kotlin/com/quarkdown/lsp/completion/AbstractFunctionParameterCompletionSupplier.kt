package com.quarkdown.lsp.completion

import com.quarkdown.core.flavor.quarkdown.QuarkdownLexerFactory
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import java.io.File

/**
 * Provides completion items for function parameters in function calls by scanning documentation files.
 * This supplier is proxied by [FunctionCompletionSupplier] and expects already-sliced text.
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
     * @param remainder the text that is being completed, typically the argument name or value that is not part
     * of a complete valid function call. For example, let `|` be the cursor position in the text,
     * `.function par|am:{...}` would have its `remainder` as `am:{...}`.
     */
    protected abstract fun getCompletionItems(
        call: WalkedFunctionCall,
        function: DocsFunction,
        remainder: String,
    ): List<CompletionItem>

    override fun getCompletionItems(
        params: CompletionParams,
        text: String,
    ): List<CompletionItem> {
        // The parsed function call associated with the token.
        val result: WalkerParsingResult<*> = getParsedFunctionCall(text) ?: return emptyList()
        val call = result.value as? WalkedFunctionCall ?: return emptyList()
        // The remainder text is the argument that is being completed, and it's not part of the parsed function call.
        val remainder = result.remainder.trim()

        // Looking up the function data from the documentation to extract available parameters to complete.
        val function: DocsFunction = getFunctionData(call.name) ?: return emptyList()

        return getCompletionItems(call, function, remainder.toString())
    }

    /**
     * Parses the function call from the given text.
     * This completion supplier is proxied by [FunctionCompletionSupplier],
     * and it's ensured that the text matches a function call.
     * @param text the text to parse
     * @return the parsed function call, or `null` if parsing fails
     */
    private fun getParsedFunctionCall(text: String): WalkerParsingResult<*>? {
        val lexer = QuarkdownLexerFactory.newInlineFunctionCallLexer(text)
        val token: Token = lexer.tokenize().singleOrNull() ?: return null
        return token.data.walkerResult
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
