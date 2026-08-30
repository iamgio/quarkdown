package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.model.CursorPosition
import com.quarkdown.lsp.model.TextPatch
import com.quarkdown.lsp.ontype.TrailingSpacesRemoverOnTypeFormattingEditSupplier
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
    ): List<TextPatch> {
        val doc = TextDocument(text.normalizeLineSeparators().toString())
        return supplier.getEdits(CursorPosition(atLine, 0), doc)
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
                .start.column,
        )
        assertEquals(
            6,
            edits
                .single()
                .end.column,
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
