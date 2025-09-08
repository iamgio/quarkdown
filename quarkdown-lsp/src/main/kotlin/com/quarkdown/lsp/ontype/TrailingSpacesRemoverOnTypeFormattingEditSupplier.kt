package com.quarkdown.lsp.ontype

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.util.getLine
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextEdit

private const val TO_REMOVE = " "

/**
 * Formatter that removes a single trailing space at the end of the previous line when the user types a newline,
 * but keeps double (or more) spaces which are significant in Markdown for hard line breaks.
 */
class TrailingSpacesRemoverOnTypeFormattingEditSupplier : OnTypeFormattingEditSupplier {
    override fun getEdits(
        params: DocumentOnTypeFormattingParams,
        document: TextDocument,
    ): List<TextEdit> {
        val lineNum = params.position.line - 1 // Line before the newline.
        val line =
            document.text.getLine(lineNum)
                ?: return emptyList() // No such line.

        if (!line.endsWith(TO_REMOVE)) return emptyList() // No trailing space.
        if (line.endsWith(TO_REMOVE + TO_REMOVE)) return emptyList() // More than one trailing space.

        val edit =
            TextEdit(
                Range(
                    Position(lineNum, line.length - TO_REMOVE.length),
                    Position(lineNum, line.length),
                ),
                "",
            )

        return listOf(edit)
    }
}
