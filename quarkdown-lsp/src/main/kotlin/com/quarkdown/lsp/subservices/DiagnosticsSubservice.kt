package com.quarkdown.lsp.subservices

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.SimpleDiagnostic

/**
 * Subservice for handling diagnostics.
 * @param diagnosticsSuppliers suppliers of diagnostic results
 */
class DiagnosticsSubservice(
    private val diagnosticsSuppliers: List<DiagnosticsSupplier>,
) : TextDocumentSubservice<Unit, List<SimpleDiagnostic>> {
    override fun process(
        params: Unit,
        document: TextDocument,
    ): List<SimpleDiagnostic> = diagnosticsSuppliers.flatMap { it.getDiagnostics(document) }
}
