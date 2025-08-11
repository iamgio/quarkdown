package com.quarkdown.lsp

import com.quarkdown.lsp.completion.FunctionCompletionSupplier
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.junit.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ALIGN_FUNCTION = "align"
private const val CLIP_FUNCTION = "clip"
private const val CSV_FUNCTION = "csv"

private const val LAYOUT_MODULE = "Layout"
private const val DATA_MODULE = "Data"

/**
 * Tests for the completion of function calls.
 */
class FunctionCompletionSupplierTest {
    private val docsDirectory = File("src/test/resources/docs")
    private val supplier = FunctionCompletionSupplier(docsDirectory)
    private val testDocumentUri = "file:///test.qd"

    private fun getCompletions(
        text: String,
        position: Position,
    ): List<CompletionItem> {
        val textDocument = TextDocumentIdentifier(testDocumentUri)
        val params = CompletionParams(textDocument, position)
        return supplier.getCompletionItems(params, text)
    }

    @Test
    fun `completions at beginning of function call`() {
        val text = "hello ."
        val completions = getCompletions(text, Position(0, text.length))

        // Verify we get all three expected completions
        assertEquals(3, completions.size)

        // Verify the expected function names are present
        val labels = completions.map { it.label }.toSet()
        assertContains(labels, ALIGN_FUNCTION)
        assertContains(labels, CLIP_FUNCTION)
        assertContains(labels, CSV_FUNCTION)

        // Verifies module names are correct.
        completions
            .filter { it.label == ALIGN_FUNCTION || it.label == CLIP_FUNCTION }
            .forEach { assertEquals(LAYOUT_MODULE, it.detail) }

        completions
            .filter { it.label == CSV_FUNCTION }
            .forEach { assertEquals(DATA_MODULE, it.detail) }
    }

    @Test
    fun `completions in middle of function name`() {
        val text = "hello .ali"
        val completions = getCompletions(text, Position(0, text.length))

        assertEquals(1, completions.size)
        assertEquals("align", completions[0].label)
        assertEquals("Layout", completions[0].detail)
    }

    @Test
    fun `no completions outside function call`() {
        val text = "hello world"
        val completions = getCompletions(text, Position(0, text.length))
        assertTrue(completions.isEmpty())
    }
}
