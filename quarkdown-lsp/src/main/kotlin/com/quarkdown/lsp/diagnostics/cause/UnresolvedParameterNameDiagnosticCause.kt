package com.quarkdown.lsp.diagnostics.cause

import com.quarkdown.quarkdoc.reader.DocsFunction
import org.eclipse.lsp4j.DiagnosticSeverity

/**
 * A diagnostic cause indicating that a parameter name used in a function call does not match any known parameters for that function.
 * @param function the function being called
 * @param parameterName the unresolved parameter name
 */
class UnresolvedParameterNameDiagnosticCause(
    private val function: DocsFunction,
    private val parameterName: String,
) : DiagnosticCause {
    override val message: String
        get() =
            """
            Unknown parameter '$parameterName' for function '${function.name}'.
            Available parameters are: ${function.parameters.joinToString(", ") { it.name }}.
            """.trimIndent()

    override val severity: DiagnosticSeverity
        get() = DiagnosticSeverity.Error
}
