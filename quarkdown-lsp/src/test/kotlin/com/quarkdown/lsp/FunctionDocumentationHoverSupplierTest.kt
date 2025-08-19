package com.quarkdown.lsp

import com.quarkdown.lsp.hover.FunctionDocumentationHoverSupplier
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.junit.Test
import java.io.File
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
        return supplier.getHover(params, text)
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
