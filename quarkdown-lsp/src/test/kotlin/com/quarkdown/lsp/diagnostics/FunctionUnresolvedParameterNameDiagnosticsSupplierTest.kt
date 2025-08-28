package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.ALIGNMENT_PARAMETER
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.ALIGN_FUNCTION
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.CSV_FUNCTION
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.DOCS_DIRECTORY
import com.quarkdown.lsp.diagnostics.DiagnosticsTestUtils.PATH_PARAMETER
import com.quarkdown.lsp.diagnostics.function.FunctionUnresolvedParameterNameDiagnosticsSupplier
import org.eclipse.lsp4j.DiagnosticSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val INVALID_PARAMETER = "invalid"

/**
 * Tests for the diagnostics supplier for unresolved parameter names in function calls.
 */
class FunctionUnresolvedParameterNameDiagnosticsSupplierTest {
    private val supplier = FunctionUnresolvedParameterNameDiagnosticsSupplier(DOCS_DIRECTORY)

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
    fun `no diagnostics for unknown function`() {
        val text = "hello .myfunc param:{value}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `no diagnostics for valid parameter name`() {
        val text = "hello .$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{center}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `error for invalid parameter name`() {
        val text = "hello .$ALIGN_FUNCTION $INVALID_PARAMETER:{center}"
        val diagnostics = getDiagnostics(text)

        assertEquals(1, diagnostics.size)
        val diagnostic = diagnostics.single()
        assertEquals(DiagnosticSeverity.Error, diagnostic.severity)
        assertEquals(text.indexOf(INVALID_PARAMETER), diagnostic.range.start)
        assertEquals(text.indexOf(INVALID_PARAMETER) + INVALID_PARAMETER.length, diagnostic.range.endInclusive)
    }

    @Test
    fun `multiple diagnostics for multiple invalid parameter names`() {
        val text = "hello .$ALIGN_FUNCTION $INVALID_PARAMETER:{center} another:{value}"
        val diagnostics = getDiagnostics(text)

        assertEquals(2, diagnostics.size)
    }

    @Test
    fun `error for one invalid parameter name among valid ones`() {
        val text = "hello .$ALIGN_FUNCTION xyz:{} $ALIGNMENT_PARAMETER:{center} $INVALID_PARAMETER:{}"
        val diagnostics = getDiagnostics(text)

        assertEquals(2, diagnostics.size)
    }

    @Test
    fun `error in nested call`() {
        val text = "hello .func {.$ALIGN_FUNCTION $INVALID_PARAMETER:{}}"
        val diagnostics = getDiagnostics(text)

        assertEquals(1, diagnostics.size)
    }

    @Test
    fun `no diagnostics for positional parameters`() {
        val text = "hello .$ALIGN_FUNCTION {center}"
        assertTrue(getDiagnostics(text).isEmpty())
    }

    @Test
    fun `chained calls should assign diagnose only for their own parameters`() {
        val text = "hello .$CSV_FUNCTION $PATH_PARAMETER:{arg}::$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{}"
        val diagnostics = getDiagnostics(text)

        assertTrue(diagnostics.isEmpty())
    }

    @Test
    fun `chained calls should diagnose unresolved parameters`() {
        val text =
            "hello .$CSV_FUNCTION $PATH_PARAMETER:{arg} $INVALID_PARAMETER:{arg}::" +
                "$ALIGN_FUNCTION $ALIGNMENT_PARAMETER:{} $INVALID_PARAMETER:{}"
        val diagnostics = getDiagnostics(text)

        assertEquals(2, diagnostics.size)
    }
}
