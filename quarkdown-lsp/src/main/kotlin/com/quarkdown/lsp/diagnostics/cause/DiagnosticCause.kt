package com.quarkdown.lsp.diagnostics.cause

import org.eclipse.lsp4j.DiagnosticSeverity

/**
 * The cause of a diagnostic.
 */
interface DiagnosticCause {
    /**
     * The message of the diagnostic.
     */
    val message: String

    /**
     * The severity of the diagnostic.
     */
    val severity: DiagnosticSeverity
}
