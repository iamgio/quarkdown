package com.quarkdown.lsp.completion

import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.lsp.documentation.htmlToMarkup
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

/**
 * Provides completion items for fixed allowed values for a function parameter, if applicable.
 * For example, let `|` be the cursor position in the text,
 * `.row alignment:{|` will provide allowed values for the `alignment` parameter of the `row` function.
 */
internal class FunctionParameterAllowedValuesCompletionSupplier(
    docsDirectory: File,
) : AbstractFunctionParameterCompletionSupplier(docsDirectory) {
    override fun getCompletionItems(
        call: WalkedFunctionCall,
        function: DocsFunction,
        remainder: String,
    ): List<CompletionItem> {
        val parameterName: String =
            QuarkdownPatterns.FunctionCall.IDENTIFIER
                .find(remainder)
                ?.groupValues
                ?.firstOrNull()
                ?: return emptyList()

        val parameter: DocsParameter =
            function.parameters
                .find { it.name == parameterName }
                ?: return emptyList()

        return parameter.allowedValues?.map {
            CompletionItem().apply {
                label = it
                documentation = Either.forRight(parameter.description.htmlToMarkup())
                kind = CompletionItemKind.Value
                insertTextFormat = InsertTextFormat.Snippet
            }
        } ?: emptyList()
    }
}
