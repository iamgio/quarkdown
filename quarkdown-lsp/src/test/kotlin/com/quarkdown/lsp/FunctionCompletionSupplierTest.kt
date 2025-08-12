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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val ALIGN_FUNCTION = "align"
private const val CLIP_FUNCTION = "clip"
private const val CSV_FUNCTION = "csv"

private const val LAYOUT_MODULE = "Layout"
private const val DATA_MODULE = "Data"

private const val ALIGNMENT_PARAMETER = "alignment"
private const val BODY_PARAMETER = "body"

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
        assertEquals(3, completions.size)

        // Verifies the expected function names are present.
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
        assertEquals(ALIGN_FUNCTION, completions.first().label)
        assertEquals(LAYOUT_MODULE, completions.first().detail)
    }

    @Test
    fun `no completions outside function call`() {
        val text = "hello world"
        val completions = getCompletions(text, Position(0, text.length))
        assertTrue(completions.isEmpty())
    }

    @Test
    fun `parameter completions at beginning of parameter list`() {
        val text = "hello .$ALIGN_FUNCTION "
        val completions = getCompletions(text, Position(0, text.length)).map { it.label }

        assertEquals(2, completions.size)
        assertContains(completions, ALIGNMENT_PARAMETER)
        assertContains(completions, BODY_PARAMETER)
    }

    @Test
    fun `parameter completions with partial parameter name`() {
        val text = "hello .$ALIGN_FUNCTION align"
        val completions = getCompletions(text, Position(0, text.length)).map { it.label }

        assertFalse(BODY_PARAMETER in completions)
        assertEquals(ALIGNMENT_PARAMETER, completions.single())
    }

    @Test
    fun `parameter completions with some parameters already specified`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{center} "
        val completions = getCompletions(text, Position(0, text.length)).map { it.label }

        assertFalse(ALIGNMENT_PARAMETER in completions)
        assertEquals(BODY_PARAMETER, completions.single())
    }

    @Test
    fun `no parameter completions when all parameters are specified`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{center} $BODY_PARAMETER:{content} "
        val completions = getCompletions(text, Position(0, text.length))
        assertTrue(completions.isEmpty())
    }
}
