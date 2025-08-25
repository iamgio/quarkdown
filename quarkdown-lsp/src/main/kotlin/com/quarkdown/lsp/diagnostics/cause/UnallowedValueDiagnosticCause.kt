package com.quarkdown.lsp.diagnostics.cause

import com.quarkdown.quarkdoc.reader.DocsParameter
import org.eclipse.lsp4j.DiagnosticSeverity

/**
 * A diagnostic cause indicating that a value is not among the allowed values (e.g. enum values) for a parameter.
 * @param parameter the parameter the value is for
 * @param value the invalid value
 */
class UnallowedValueDiagnosticCause(
    private val parameter: DocsParameter,
    private val value: String,
) : DiagnosticCause {
    override val message: String
        get() =
            """
            Invalid value '$value' for parameter '${parameter.name}'.
            Allowed values are: ${parameter.allowedValues!!.joinToString(", ")}
            """.trimIndent()

    override val severity: DiagnosticSeverity
        get() = DiagnosticSeverity.Warning
}
