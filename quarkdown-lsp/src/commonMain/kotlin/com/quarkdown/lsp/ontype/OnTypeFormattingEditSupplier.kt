package com.quarkdown.lsp.ontype

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.TextPatch

/**
 * Supplier of text edits for on-type formatting.
 */
interface OnTypeFormattingEditSupplier {
    /**
     * Provides text edits for on-type formatting.
     * @param position the position of the cursor after the typed character
     * @param document the text document to format
     * @return a list of text patches to apply to the document
     */
    fun getEdits(
        position: CursorPosition,
        document: TextDocument,
    ): List<TextPatch>
}
