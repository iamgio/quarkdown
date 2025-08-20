package com.quarkdown.lsp.hover

import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import com.quarkdown.lsp.tokenizer.getAtSourceIndex
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
        text: String,
    ): Hover? {
        // Gets the function call at the specified hover position.
        val index = params.position.toOffset(text)
        val call: FunctionCall =
            FunctionCallTokenizer().getFunctionCalls(text).getAtSourceIndex(index)
                ?: return null

        // Returns the documentation to display in the hover.
        val function: DocumentedFunction =
            call.getDocumentation(docsDirectory)
                ?: return null

        return Hover(function.documentationAsMarkup)
    }
}
