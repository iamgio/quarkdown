package com.quarkdown.lsp.ontype

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.TextPatch
import com.quarkdown.lsp.util.getLine

private const val TO_REMOVE = " "

/**
 * Formatter that removes a single trailing space at the end of the previous line when the user types a newline,
 * but keeps double (or more) spaces which are significant in Markdown for hard line breaks.
 */
class TrailingSpacesRemoverOnTypeFormattingEditSupplier : OnTypeFormattingEditSupplier {
    override fun getEdits(
        position: CursorPosition,
        document: TextDocument,
    ): List<TextPatch> {
        val lineNum = position.line - 1 // Line before the newline.
        val line =
            document.text.getLine(lineNum)
                ?: return emptyList() // No such line.

        if (!line.endsWith(TO_REMOVE)) return emptyList() // No trailing space.
        if (line.endsWith(TO_REMOVE + TO_REMOVE)) return emptyList() // More than one trailing space.

        val edit =
            TextPatch(
                start = CursorPosition(lineNum, line.length - TO_REMOVE.length),
                end = CursorPosition(lineNum, line.length),
                text = "",
            )

        return listOf(edit)
    }
}
