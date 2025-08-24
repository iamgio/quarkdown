package com.quarkdown.lsp.completion.function.parameter

import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.completion.function.AbstractFunctionCompletionSupplier
import com.quarkdown.lsp.completion.toCompletionItem
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.getTokenAtSourceIndex
import com.quarkdown.lsp.util.remainderUntilIndex
import org.eclipse.lsp4j.CompletionItem
import java.io.File

/**
 * Provides completion items for function parameter names. For example, let `|` be the cursor position in the text,
 * `.function pa|` will provide names for parameters starting with `pa`.
 */
class FunctionParameterNameCompletionSupplier(
    docsDirectory: File,
) : AbstractFunctionCompletionSupplier(docsDirectory) {
    /**
     * Transforms the cursor index to the index of the last whitespace before the cursor,
     * so that the returned index is always part of the function call.
     *
     * Note that, by design, an inline argument without both delimiters is not part of the function call.
     */
    override fun transformIndex(
        cursorIndex: Int,
        text: String,
    ): Int =
        text
            .substring(0, cursorIndex)
            .indexOfLast { it.isWhitespace() }

    override fun getCompletionItems(
        call: FunctionCall,
        function: DocumentedFunction?,
        cursorIndex: Int,
        originalCursorIndex: Int,
    ): List<CompletionItem> {
        if (function == null) return emptyList()
        // Parameter names are only completed when the parameter name is being typed, so it's not yet part of the function call.
        if (call.getTokenAtSourceIndex(originalCursorIndex) != null) return emptyList()

        // The remainder of the function call before the cursor position.
        // For example, if the function call being completed is `.function param`,
        // the remainder is `param`.
        val remainder = call.remainderUntilIndex(originalCursorIndex)?.trim() ?: ""

        val arguments = call.parserResult.value.arguments

        return function.data.parameters
            .asSequence()
            .filter { it.name.startsWith(remainder) }
            .filter { param -> arguments.none { arg -> arg.name == param.name } } // Exclude already present parameters
            .map { it.toCompletionItem() }
            .toList()
    }
}
