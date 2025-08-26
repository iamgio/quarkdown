package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.ALIGNMENT_PARAMETER
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.ALIGN_FUNCTION
import com.quarkdown.lsp.diagnostics.function.FunctionDuplicateParameterNameDiagnosticsSupplier
import org.eclipse.lsp4j.DiagnosticSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the diagnostics supplier for duplicate parameter names in function calls.
 */
class FunctionDuplicateParameterNameDiagnosticsSupplierTest {
    private val supplier = FunctionDuplicateParameterNameDiagnosticsSupplier()

    private fun getDiagnostics(text: String): List<SimpleDiagnostic> = DiagnosticsTestUtils.getDiagnostics(text, supplier)

    @Test
    fun `no diagnostics in empty text`() {
        assertTrue(getDiagnostics("").isEmpty())
    }

    @Test
    fun `no diagnostics in non-function text`() {
        val text = "hello world"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `no diagnostics for function with no parameters`() {
        val text = "hello .$ALIGN_FUNCTION"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `no diagnostics for function with unique parameter names`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{center} another:{value}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `error for function with duplicate parameter names`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{center} $ALIGNMENT_PARAMETER:{start}"
        val diagnostics = getDiagnostics(text)

        assertEquals(2, diagnostics.size)

        diagnostics.forEach { diagnostic ->
            assertEquals(DiagnosticSeverity.Error, diagnostic.severity)
            assertTrue(diagnostic.range.start >= text.indexOf(ALIGNMENT_PARAMETER))
            assertTrue(diagnostic.range.endInclusive <= text.length)
        }
    }

    @Test
    fun `error for function with multiple duplicate parameter names`() {
        val text = "hello .$ALIGN_FUNCTION param:{value} param:{other} another:{x} another:{y}"
        val diagnostics = getDiagnostics(text)

        assertEquals(4, diagnostics.size)
    }

    @Test
    fun `error for function with more than two duplicates of a parameter name`() {
        val text = "hello .$ALIGN_FUNCTION param:{value} param:{other} param:{x}"
        val diagnostics = getDiagnostics(text)

        assertEquals(3, diagnostics.size)
    }

    @Test
    fun `error for duplicate parameter names in nested function call`() {
        val text = "hello .func {.$ALIGN_FUNCTION param:{value} param:{other}}"
        val diagnostics = getDiagnostics(text)

        assertEquals(2, diagnostics.size)
    }

    @Test
    fun `error also for unresolved functions`() {
        val text = "hello .myfunc param:{value} param:{other}"
        val diagnostics = getDiagnostics(text)

        assertEquals(2, diagnostics.size)
    }
}
