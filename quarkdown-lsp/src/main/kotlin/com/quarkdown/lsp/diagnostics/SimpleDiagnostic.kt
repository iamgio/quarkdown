package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.diagnostics.cause.DiagnosticCause
import com.quarkdown.lsp.model.Severity

/**
 * A simple diagnostic that can be sent to the client.
 * @param range the range of the diagnostic in the source text
 * @param message the message of the diagnostic
 * @param severity the severity of the diagnostic
 */
data class SimpleDiagnostic(
    val range: IntRange,
    val message: String,
    val severity: Severity,
) {
    /**
     * Creates a [SimpleDiagnostic] from a [DiagnosticCause].
     * @param range the range of the diagnostic in the source text
     * @param cause the cause of the diagnostic
     */
    constructor(range: IntRange, cause: DiagnosticCause) : this(range, cause.message, cause.severity)
}
