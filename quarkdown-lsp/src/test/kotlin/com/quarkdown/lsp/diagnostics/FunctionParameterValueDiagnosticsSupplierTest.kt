package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.ALIGNMENT_PARAMETER
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.ALIGN_FUNCTION
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.DOCS_DIRECTORY
import com.quarkdown.lsp.diagnostics.function.FunctionParameterValueDiagnosticsSupplier
import org.eclipse.lsp4j.DiagnosticSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the diagnostics supplier for function calls.
 */
class FunctionParameterValueDiagnosticsSupplierTest {
    private val supplier = FunctionParameterValueDiagnosticsSupplier(DOCS_DIRECTORY)

    private fun getDiagnostics(text: String): List<SimpleDiagnostic> = DiagnosticsTestUtils.getDiagnostics(text, supplier)

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

    @Test
    fun `no diagnostics for value of nested function call`() {
        val text = "hello .$ALIGN_FUNCTION {.func}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `no diagnostics for value containing nested function call`() {
        val text = "hello .$ALIGN_FUNCTION {a .func b}"
        assertTrue(getDiagnostics(text).isEmpty())
    }
}
