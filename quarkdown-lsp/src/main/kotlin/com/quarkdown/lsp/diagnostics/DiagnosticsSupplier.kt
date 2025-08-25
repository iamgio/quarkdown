package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.TextDocument
import org.eclipse.lsp4j.Diagnostic

/**
 *
 */
interface DiagnosticsSupplier {
    /**
     * Generates a list of diagnostics.
     * @param document the current document
     * @return a list of diagnostics that can be reported
     */
    fun getDiagnostics(document: TextDocument): List<Diagnostic>
}
