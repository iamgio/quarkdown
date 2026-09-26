package com.quarkdown.lsp.hover

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.HoverInfo

/**
 * Interface for providing hover information based on the current context in a text document.
 */
interface HoverSupplier {
    /**
     * Generates hover information.
     * @param position the position in the document the hover was requested at
     * @param document the current document
     * @return the hover information, or `null` if none is available
     */
    fun getHover(
        position: CursorPosition,
        document: TextDocument,
    ): HoverInfo?
}
