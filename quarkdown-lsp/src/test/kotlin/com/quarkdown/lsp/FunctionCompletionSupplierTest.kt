package com.quarkdown.lsp

import com.quarkdown.lsp.completion.function.FunctionCompletionSupplier
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val ALIGN_FUNCTION = "align"
private const val CLIP_FUNCTION = "clip"
private const val COLUMN_FUNCTION = "column"
private const val CSV_FUNCTION = "csv"

private const val LAYOUT_MODULE = "Layout"
private const val DATA_MODULE = "Data"

private const val ALIGNMENT_PARAMETER = "alignment"
private const val BODY_PARAMETER = "body"
private const val CAPTION_PARAMETER = "caption"

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
    fun `no completions at empty position`() {
        val text = ""
        val completions = getCompletions(text, Position(0, 0))
        assertTrue(completions.isEmpty())
    }

    @Test
    fun `no completions outside function call`() {
        val text = "hello world"
        val completions = getCompletions(text, Position(0, text.length))
        assertTrue(completions.isEmpty())
    }

    @Test
    fun `no completions in invalid function call position`() {
        val text = "hello."
        val position = Position(0, text.length)
        val completions = getCompletions(text, position)
        assertTrue(completions.isEmpty())
    }

    @Test
    fun `completions at beginning of function call`() {
        val text = "hello ."
        val completions = getCompletions(text, Position(0, text.length))
        assertEquals(4, completions.size)

        // Verifies the expected function names are present.
        val labels = completions.map { it.label }.toSet()
        assertContains(labels, ALIGN_FUNCTION)
        assertContains(labels, CLIP_FUNCTION)
        assertContains(labels, COLUMN_FUNCTION)
        assertContains(labels, CSV_FUNCTION)

        // Verifies module names are correct.
        completions
            .filter { it.label == ALIGN_FUNCTION || it.label == CLIP_FUNCTION }
            .forEach { assertEquals(LAYOUT_MODULE, it.detail) }

        completions
            .filter { it.label == CSV_FUNCTION }
            .forEach { assertEquals(DATA_MODULE, it.detail) }

        // Verifies the insertion snippet is correct.

        // Only mandatory parameters.
        val alignCompletion = completions.first { it.label == ALIGN_FUNCTION }
        assertEquals(
            "align {\${1:alignment (start|center|end)}} \n    \${2:body}",
            alignCompletion.insertText,
        )

        // One optional parameter.
        val csvCompletion = completions.first { it.label == CSV_FUNCTION }
        assertEquals(
            "csv {\${1:path}} ",
            csvCompletion.insertText,
        )

        // Only optional parameters + body.
        val columnCompletion = completions.first { it.label == COLUMN_FUNCTION }
        assertEquals(
            "column \${1:}\n    \${2:body}",
            columnCompletion.insertText,
        )
    }

    @Test
    fun `name completions for partial function name`() {
        val text = "hello .ali"
        val completions = getCompletions(text, Position(0, text.length))

        assertEquals(1, completions.size)
        assertEquals(ALIGN_FUNCTION, completions.first().label)
        assertEquals(LAYOUT_MODULE, completions.first().detail)
    }

    @Test
    fun `name completion in chain for empty name`() {
        val text = "hello .$ALIGN_FUNCTION::"
        val completions = getCompletions(text, Position(0, text.length))
        assertEquals(4, completions.size)
    }

    @Test
    fun `name completion in chain with args`() {
        val text = "hello .$ALIGN_FUNCTION {arg}::"
        val completions = getCompletions(text, Position(0, text.length))
        assertEquals(4, completions.size)
    }

    @Test
    fun `name completion in chain for partial function name`() {
        val text = "hello .$ALIGN_FUNCTION::c"
        val completions = getCompletions(text, Position(0, text.length)).map { it.label }

        assertEquals(3, completions.size)
        assertContains(completions, CSV_FUNCTION)
        assertContains(completions, COLUMN_FUNCTION)
        assertContains(completions, CLIP_FUNCTION)
    }

    @Test
    fun `name completion in chain should skip first parameter`() {
        val text = "hello .$ALIGN_FUNCTION::cs"
        val completions = getCompletions(text, Position(0, text.length))

        assertEquals(1, completions.size)

        val completion = completions.single()
        assertEquals(CSV_FUNCTION, completion.label)
        // The call is chained, so the first parameter (which should be the path) is skipped.
        assertEquals(CSV_FUNCTION, completion.insertText.trim())
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
    fun `parameter completions after line break`() {
        val text = "abc\n\n.$ALIGN_FUNCTION "
        val position = Position(2, text.lines().last().length)
        val completions = getCompletions(text, position).map { it.label }

        assertContains(completions, ALIGNMENT_PARAMETER)
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

    @Test
    fun `parameter completions in nested function call`() {
        val text = "hello .func {.$ALIGN_FUNCTION al}"
        val completions = getCompletions(text, Position(0, text.length - 1)).map { it.label }

        assertEquals(ALIGNMENT_PARAMETER, completions.single())
    }

    @Test
    fun `parameter completions with nested function call`() {
        val text = "hello .$CSV_FUNCTION {.func {arg}} capt"
        val completions = getCompletions(text, Position(0, text.length)).map { it.label }

        assertEquals(CAPTION_PARAMETER, completions.single())
    }

    @Test
    fun `parameter value, empty argument`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{}"
        val completions = getCompletions(text, Position(0, text.length - 1)).map { it.label }

        assertContains(completions, "start")
        assertContains(completions, "center")
        assertContains(completions, "end")
    }

    @Test
    fun `parameter value, partial argument`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{cen}"
        val completions = getCompletions(text, Position(0, text.length - 1)).map { it.label }

        assertEquals(completions.single(), "center")
    }
}
