package com.quarkdown.lsp.completion

import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.lsp.documentation.htmlToMarkup
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.File

private const val REQUIRED = "required"

/**
 * Provides completion items for function parameter names. For example, let `|` be the cursor position in the text,
 * `.function pa|` will provide names for parameters starting with `pa`.
 */
internal class FunctionParameterNameCompletionSupplier(
    docsDirectory: File,
) : AbstractFunctionParameterCompletionSupplier(docsDirectory) {
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
            insertText = FunctionCallInsertionSnippet.forParameter(this@toCompletionItem, alwaysNamed = true)
        }

    override fun getCompletionItems(
        call: WalkedFunctionCall,
        function: DocsFunction,
        remainder: String,
    ): List<CompletionItem> =
        function.parameters
            .asSequence()
            .filter { it.name.startsWith(remainder) }
            .filter { param -> call.arguments.none { arg -> arg.name == param.name } } // Exclude already present parameters
            .map { it.toCompletionItem() }
            .toList()
}
