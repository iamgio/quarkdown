package com.quarkdown.lsp

import com.quarkdown.lsp.hover.function.FunctionDocumentationHoverSupplier
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val ALIGN_FUNCTION = "align"
private const val CSV_FUNCTION = "csv"

/**
 * Tests for [FunctionDocumentationHoverSupplier].
 */
class FunctionDocumentationHoverSupplierTest {
    private val testDocsDirectory = File("src/test/resources/docs")
    private val supplier = FunctionDocumentationHoverSupplier(testDocsDirectory)

    /**
     * Helper function to get hover information for a given text and position.
     */
    private fun getHover(
        text: String,
        position: Position,
    ): Hover? {
        val params = HoverParams()
        params.position = position
        val document = TextDocument(text = text)
        return supplier.getHover(params, document)
    }

    @Test
    fun `hover outside function call returns null`() {
        val text = "This is a test with a .$ALIGN_FUNCTION call."
        val position = Position(0, 5)
        assertNull(getHover(text, position))
    }

    @Test
    fun `hover over function call`() {
        val text = "This is a test with a .$ALIGN_FUNCTION call."
        val position = Position(0, text.indexOf(ALIGN_FUNCTION) + ALIGN_FUNCTION.length / 2)

        val hover = getHover(text, position)

        assertNotNull(hover)
        assertContains(
            hover.contents.right.value,
            "#### Parameters",
        )
    }

    @Test
    fun `hover over function call argument`() {
        val text = "This is a test with a .$ALIGN_FUNCTION {center} call."
        val position = Position(0, text.indexOf("center"))
        assertNotNull(getHover(text, position))
    }

    @Test
    fun `hover over chained function call`() {
        val text = "This is a test with a .$ALIGN_FUNCTION::$CSV_FUNCTION {arg}"

        val csvHover = getHover(text, Position(0, text.length - 1))
        val alignHover = getHover(text, Position(0, text.indexOf(ALIGN_FUNCTION) + ALIGN_FUNCTION.length / 2))

        assertNotNull(csvHover)
        assertContains(
            csvHover.contents.right.value,
            CSV_FUNCTION,
        )

        assertNotNull(alignHover)
        assertContains(
            alignHover.contents.right.value,
            ALIGN_FUNCTION,
        )
    }

    @Test
    fun `hover over nested function call`() {
        val text = "This is a test with a .$ALIGN_FUNCTION {.$CSV_FUNCTION} call."
        val alignPosition = Position(0, text.indexOf(ALIGN_FUNCTION))
        val csvPosition = Position(0, text.indexOf(CSV_FUNCTION))

        val alignHover = getHover(text, alignPosition)
        val csvHover = getHover(text, csvPosition)

        assertNotNull(alignHover)
        assertContains(
            alignHover.contents.right.value,
            ".$ALIGN_FUNCTION",
        )

        assertNotNull(csvHover)
        assertContains(
            csvHover.contents.right.value,
            ".$CSV_FUNCTION",
        )
    }
}
