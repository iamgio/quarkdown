package com.quarkdown.lsp.completion.function.impl.parameter

import com.quarkdown.core.util.substringWithinBounds
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.function.FunctionCallInsertionSnippet
import com.quarkdown.lsp.documentation.htmlToMarkup
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.util.remainderUntilIndex
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

    /**
     * Transforms the cursor index to the index of the last whitespace before the cursor,
     * so that the returned index is always part of the function call.
     *
     * Note that, by design, an inline argument without both delimiters is not part of the function call.
     */
    override fun transformIndex(
        cursorIndex: Int,
        text: String,
    ): Int? =
        text
            .substringWithinBounds(0, cursorIndex)
            .indexOfLast { it.isWhitespace() }
            .takeIf { it >= 0 }

    override fun getCompletionItems(
        call: FunctionCall,
        function: DocumentedFunction,
        cursorIndex: Int,
    ): List<CompletionItem> {
        // The remainder of the function call before the cursor position.
        // For example, if the function call being completed is `.function param`,
        // the remainder is `param`.
        val remainder = call.parserResult.remainderUntilIndex(cursorIndex).trim()

        val arguments = call.parserResult.value.arguments

        return function.data.parameters
            .asSequence()
            .filter { it.name.startsWith(remainder) }
            .filter { param -> arguments.none { arg -> arg.name == param.name } } // Exclude already present parameters
            .map { it.toCompletionItem() }
            .toList()
    }
}
