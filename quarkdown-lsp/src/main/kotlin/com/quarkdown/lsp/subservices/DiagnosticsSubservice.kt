package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import org.eclipse.lsp4j.Diagnostic

/**
 * Subservice for handling diagnostics.
 * @param diagnosticsSuppliers suppliers of diagnostic results
 */
class DiagnosticsSubservice(
    private val diagnosticsSuppliers: List<DiagnosticsSupplier>,
) : TextDocumentSubservice<Any?, List<Diagnostic>> {
    override fun process(
        params: Any?,
        document: TextDocument,
    ): List<Diagnostic> = diagnosticsSuppliers.flatMap { it.getDiagnostics(document) }
}
