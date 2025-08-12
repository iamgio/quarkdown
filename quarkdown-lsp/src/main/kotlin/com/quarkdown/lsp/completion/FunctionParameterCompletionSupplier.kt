package com.quarkdown.lsp.completion

import com.quarkdown.core.flavor.quarkdown.QuarkdownLexerFactory
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.lsp.documentation.htmlToMarkup
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

private const val REQUIRED = "required"

/**
 * Provides completion items for function parameters in function calls by scanning documentation files.
 * This supplier is proxied by [FunctionCompletionSupplier] and expects already-sliced text.
 * @param docsDirectory the directory containing the documentation files to extract function data from
 */
internal class FunctionParameterCompletionSupplier(
    private val docsDirectory: File,
) : CompletionSupplier {
    /**
     * Converts a [DocsParameter] to a [CompletionItem] for use in parameter completion.
     */
    private fun DocsParameter.toCompletionItem() =
        CompletionItem().apply {
            label = name
            detail = if (!isOptional) REQUIRED else null
            documentation = Either.forRight(description.htmlToMarkup())
            kind = CompletionItemKind.Field
            insertTextFormat = InsertTextFormat.Snippet
            insertText =
                with(QuarkdownPatterns.FunctionCall) {
                    "$name$NAMED_ARGUMENT_DELIMITER$ARGUMENT_BEGIN\${1:}$ARGUMENT_END "
                }
        }

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

        val parameters: Sequence<DocsParameter> =
            function
                .parameters
                .asSequence()
                .filter { it.name.startsWith(remainder) }
                .filter { param -> call.arguments.none { arg -> arg.name == param.name } } // Exclude already present parameters

        return parameters
            .map { it.toCompletionItem() }
            .toList()
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
