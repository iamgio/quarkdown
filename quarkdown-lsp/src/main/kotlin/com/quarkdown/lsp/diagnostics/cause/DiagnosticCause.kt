package com.quarkdown.lsp.diagnostics.cause

import com.quarkdown.lsp.model.Severity

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
    val severity: Severity
}
