package com.quarkdown.lsp.hover

import com.quarkdown.lsp.TextDocument
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams

/**
 * Interface for providing hover information based on the current context in a text document.
 */
interface HoverSupplier {
    /**
     * Generates a hover object.
     * @param params the parameters for the hover request, including the position in the document
     * @param document the current document
     * @return a [Hover] object containing the hover information, or `null` if no hover information is available
     */
    fun getHover(
        params: HoverParams,
        document: TextDocument,
    ): Hover?
}
