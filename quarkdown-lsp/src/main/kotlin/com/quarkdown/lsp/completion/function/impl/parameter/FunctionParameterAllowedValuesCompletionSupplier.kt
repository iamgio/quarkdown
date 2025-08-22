package com.quarkdown.lsp.completion.function.impl.parameter

import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.findMatchingTokenBeforeIndex
import com.quarkdown.lsp.tokenizer.getTokenAtSourceIndex
import com.quarkdown.quarkdoc.reader.DocsParameter
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat
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
        call: FunctionCall,
        function: DocumentedFunction,
        cursorIndex: Int,
        originalCursorIndex: Int,
    ): List<CompletionItem> {
        // If a value is partially present, it can be completed.
        // If no value is present, all allowed values are returned.
        val value: String =
            call
                .getTokenAtSourceIndex(cursorIndex)
                ?.takeIf { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_VALUE }
                ?.lexeme
                ?.trim()
                ?: ""

        // The name of the parameter that refers to the value being completed.
        // For example, in `.row alignment:{...}`, the parameter name is `alignment`.
        val parameterName: String = getParameterName(call.tokens, cursorIndex) ?: return emptyList()

        // The parameter data looked up from documentation.
        val parameter: DocsParameter =
            function.data.parameters
                .find { it.name == parameterName }
                ?: return emptyList()

        return parameter.allowedValues
            ?.filter { it.startsWith(value) }
            ?.map {
                CompletionItem().apply {
                    label = it
                    kind = CompletionItemKind.Value
                    insertTextFormat = InsertTextFormat.Snippet
                }
            }
            ?: emptyList()
    }

    /**
     * Extracts the name of the parameter being completed based on the cursor position.
     * It iterates through the tokens of the function call and identifies the parameter name
     * based on the token type and its range.
     *
     * For example, consider the function call `.row alignment:{|}` with the cursor at `|`,
     * then this function will return `alignment` as the parameter name.
     * @param tokens the list of tokens in the function call
     * @param cursorIndex the index of the cursor in the source text (which is supposed to be in the argument value)
     * @return the name of the parameter, if any
     */
    private fun getParameterName(
        tokens: List<FunctionCallToken>,
        cursorIndex: Int,
    ): String? =
        tokens
            .findMatchingTokenBeforeIndex(
                beforeIndex = cursorIndex,
                matchType = FunctionCallToken.Type.PARAMETER_NAME,
                reset = setOf(FunctionCallToken.Type.INLINE_ARGUMENT_END),
            )?.lexeme
}
