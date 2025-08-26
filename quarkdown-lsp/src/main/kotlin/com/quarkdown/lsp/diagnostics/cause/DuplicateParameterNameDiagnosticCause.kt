package com.quarkdown.lsp.diagnostics.cause

import org.eclipse.lsp4j.DiagnosticSeverity

/**
 * A diagnostic cause indicating that a parameter name appears multiple times in a function call.
 * @param parameterName the duplicate parameter name
 */
class DuplicateParameterNameDiagnosticCause(
    private val parameterName: String,
) : DiagnosticCause {
    override val message: String
        get() = "The parameter name '$parameterName' appears multiple times in the same function call."

    override val severity: DiagnosticSeverity
        get() = DiagnosticSeverity.Error
}
