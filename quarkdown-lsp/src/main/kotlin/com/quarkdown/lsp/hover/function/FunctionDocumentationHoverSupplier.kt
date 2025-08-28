package com.quarkdown.lsp.hover.function

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.hover.HoverSupplier
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
import com.quarkdown.lsp.tokenizer.getTokenAtSourceIndex
import com.quarkdown.lsp.util.toOffset
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import java.io.File

/**
 * Provider of documentation on hover for function calls.
 * @property docsDirectory the directory containing the documentation files
 */
class FunctionDocumentationHoverSupplier(
    private val docsDirectory: File,
) : HoverSupplier {
    override fun getHover(
        params: HoverParams,
        document: TextDocument,
    ): Hover? {
        val text = document.text

        // Gets the function call at the specified hover position.
        val index = params.position.toOffset(text)
        val call: FunctionCall =
            document.functionCalls
                .getAtSourceIndex(index)
                ?: return null

        // If the hover position is over a function name in the chain, shows documentation for that specific function.
        // Otherwise, shows documentation for the last function in the chain.
        val nameToken: FunctionCallToken? =
            call
                .getTokenAtSourceIndex(index)
                ?.takeIf { it.type == FunctionCallToken.Type.FUNCTION_NAME }

        // Returns the documentation to display in the hover.
        val function: DocumentedFunction =
            getDocumentation(docsDirectory, nameToken?.lexeme ?: call.lastChainedName)
                ?: return null

        return Hover(function.documentationAsMarkup)
    }
}
