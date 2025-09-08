package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.ontype.TrailingSpacesRemoverOnTypeFormattingEditSupplier
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextEdit
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [TrailingSpacesRemoverOnTypeFormattingEditSupplier].
 */
class TrailingSpacesRemoverFormattingEditSupplierTest {
    private val supplier = TrailingSpacesRemoverOnTypeFormattingEditSupplier()

    private fun getEdits(
        text: String,
        atLine: Int,
    ): List<TextEdit> {
        val doc = TextDocument(text.normalizeLineSeparators().toString())
        val options = FormattingOptions(2, true)
        val params =
            DocumentOnTypeFormattingParams(TextDocumentIdentifier("mem://test.md"), options, Position(atLine, 0), "\n")
        return supplier.getEdits(params, doc)
    }

    @Test
    fun `removes single trailing space`() {
        val text = "Hello \n"
        val edits = getEdits(text, 1)
        assertEquals(1, edits.size)
        assertEquals(
            5,
            edits
                .single()
                .range.start.character,
        )
        assertEquals(
            6,
            edits
                .single()
                .range.end.character,
        )
    }

    @Test
    fun `removes single trailing spaces among multiple lines`() {
        val text = "Hello \nWorld \nThis is a test \n"
        val edits = getEdits(text, 3)
        assertEquals(1, edits.size)
    }

    @Test
    fun `keeps double trailing space`() {
        val text = "Hello  \n"
        assertEquals(0, getEdits(text, 1).size)
    }

    @Test
    fun `no trailing space to remove`() {
        val text = "Hello\n"
        assertEquals(0, getEdits(text, 1).size)
    }
}
