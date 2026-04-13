package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.ontype.LineContinuationAutoIndentOnTypeFormattingEditSupplier
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextEdit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [LineContinuationAutoIndentOnTypeFormattingEditSupplier].
 */
class LineContinuationAutoIndentOnTypeFormattingEditSupplierTest {
    private val supplier = LineContinuationAutoIndentOnTypeFormattingEditSupplier()

    private fun getEdits(
        text: String,
        atLine: Int,
    ): List<TextEdit> {
        val doc = TextDocument(text.normalizeLineSeparators().toString())
        val options = FormattingOptions(2, true)
        val params =
            DocumentOnTypeFormattingParams(TextDocumentIdentifier("mem://test.qd"), options, Position(atLine, 0), "\n")
        return supplier.getEdits(params, doc)
    }

    @Test
    fun `indents after line continuation in function call`() {
        // .container alignment:{center} \
        // <cursor here>
        val text = ".container alignment:{center} \\\n"
        val edits = getEdits(text, 1)
        assertEquals(1, edits.size)
        val edit = edits.single()
        // Inserts indentation to align with the first argument (after `.container `).
        assertEquals(1, edit.range.start.line)
        assertEquals(0, edit.range.start.character)
        assertEquals(" ".repeat(".container ".length), edit.newText)
    }

    @Test
    fun `indents after line continuation with existing args`() {
        // .func {arg1} \
        // <cursor here>
        val text = ".func {arg1} \\\n"
        val edits = getEdits(text, 1)
        assertEquals(1, edits.size)
        assertEquals(" ".repeat(".func ".length), edits.single().newText)
    }

    @Test
    fun `indents after line continuation with no args`() {
        // .func \
        // <cursor here>
        val text = ".func \\\n"
        val edits = getEdits(text, 1)
        assertEquals(1, edits.size)
        assertEquals(" ".repeat(".func ".length), edits.single().newText)
    }

    @Test
    fun `indents after line continuation in inline function preceded by other content`() {
        // Hello .func {arg1} \
        // <cursor here>
        val text = "Hello .func {arg1} \\\n"
        val edits = getEdits(text, 1)
        assertEquals(1, edits.size)
        assertEquals(" ".repeat("Hello .func ".length), edits.single().newText)
    }

    @Test
    fun `no indentation when previous line does not end with backslash`() {
        val text = ".func {arg1}\n"
        val edits = getEdits(text, 1)
        assertTrue(edits.isEmpty())
    }

    @Test
    fun `no indentation for non-function-call line`() {
        val text = "Hello world \\\n"
        val edits = getEdits(text, 1)
        assertTrue(edits.isEmpty())
    }

    @Test
    fun `no indentation on subsequent continuation lines`() {
        // .container alignment:{center} \
        //            padding:{1px} \
        // <cursor here>
        // The editor's built-in auto-indent preserves the previous line's indentation.
        val text = ".container alignment:{center} \\\n           padding:{1px} \\\n"
        assertTrue(getEdits(text, 2).isEmpty())
    }

    @Test
    fun `no indentation on third continuation line`() {
        val indent = " ".repeat(".container ".length)
        val text = ".container alignment:{center} \\\n${indent}padding:{1px} \\\n${indent}margin:{2px} \\\n"
        assertTrue(getEdits(text, 3).isEmpty())
    }
}
