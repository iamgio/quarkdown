package com.quarkdown.lsp.diagnostics.function

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import com.quarkdown.lsp.util.offsetToPosition
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Range
import java.io.File

/**
 *
 */
class FunctionParameterValueDiagnosticsSupplier(
    private val docsDirectory: File,
) : DiagnosticsSupplier {
    override fun getDiagnostics(document: TextDocument): List<Diagnostic> {
        val text = document.text
        val calls = document.functionCalls
        return calls.map {
            Diagnostic().apply {
                range = Range(offsetToPosition(text, it.range.start), offsetToPosition(text, it.range.endInclusive))
                message = "Testing"
                severity = DiagnosticSeverity.Warning
            }
        }
    }
}
