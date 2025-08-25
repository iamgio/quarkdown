package com.quarkdown.lsp.diagnostics.function

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.SimpleDiagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import java.io.File

/**
 *
 */
class FunctionParameterValueDiagnosticsSupplier(
    private val docsDirectory: File,
) : DiagnosticsSupplier {
    override fun getDiagnostics(document: TextDocument): List<SimpleDiagnostic> {
        val calls = document.functionCalls
        return calls.map {
            SimpleDiagnostic(
                range = it.range,
                message = "Testing",
                severity = DiagnosticSeverity.Warning,
            )
        }
    }
}
