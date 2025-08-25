package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.diagnostics.function.FunctionParameterValueDiagnosticsSupplier
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ALIGN_FUNCTION = "align"
private const val ALIGNMENT_PARAMETER = "alignment"

/**
 * Tests for the diagnostics supplier for function calls.
 */
class FunctionParameterValueDiagnosticsSupplierTest {
    private val docsDirectory = File("src/test/resources/docs")
    private val supplier = FunctionParameterValueDiagnosticsSupplier(docsDirectory)
    private val testDocumentUri = "file:///test.qd"

    private fun getDiagnostics(text: String): List<SimpleDiagnostic> {
        val textDocument = TextDocumentIdentifier(testDocumentUri)
        val document = TextDocument(text = text)
        return supplier.getDiagnostics(document)
    }

    @Test
    fun `no diagnostics in empty text`() {
        assertTrue(getDiagnostics("").isEmpty())
    }

    @Test
    fun `no diagnostics in non-restricted function`() {
        val text = "hello .csv {somevalue}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `no diagnostics for correct value, parameter by name`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{center}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `no diagnostics for correct value, parameter by position`() {
        val text = "hello .$ALIGN_FUNCTION {center}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `warning in parameter by name, partial value`() {
        val value = "cen"
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{$value}"
        val diagnostics = getDiagnostics(text)

        assertEquals(1, diagnostics.size)
        val diagnostic = diagnostics.single()
        assertEquals(DiagnosticSeverity.Warning, diagnostic.severity)
        assertEquals(text.indexOf(value), diagnostic.range.start)
        assertEquals(text.length - 1, diagnostic.range.endInclusive)
    }

    @Test
    fun `warning in parameter by position, partial value`() {
        val text = "hello .$ALIGN_FUNCTION {cen}"
        val diagnostics = getDiagnostics(text)
        assertEquals(1, diagnostics.size)
    }
}
