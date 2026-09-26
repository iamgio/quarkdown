package com.quarkdown.lsp.diagnostics.cause

import com.quarkdown.lsp.model.Severity

/**
 * A diagnostic cause indicating that a parameter name appears multiple times in a function call.
 * @param parameterName the duplicate parameter name
 */
class DuplicateParameterNameDiagnosticCause(
    private val parameterName: String,
) : DiagnosticCause {
    override val message: String
        get() = "The parameter name '$parameterName' appears multiple times in the same function call."

    override val severity: Severity
        get() = Severity.ERROR
}
