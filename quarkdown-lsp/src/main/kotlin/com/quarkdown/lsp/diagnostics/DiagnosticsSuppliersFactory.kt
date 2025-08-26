package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.QuarkdownLanguageServer
import com.quarkdown.lsp.diagnostics.function.FunctionParameterValueDiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.function.FunctionUnresolvedParameterNameDiagnosticsSupplier

/**
 * Factory for creating a list of [DiagnosticsSupplier]s.
 */
object DiagnosticsSuppliersFactory {
    /**
     * @param server the Quarkdown language server instance
     * @return the default list of [DiagnosticsSuppliersFactory] instances
     */
    fun default(server: QuarkdownLanguageServer): List<DiagnosticsSupplier> {
        val docsDirectory = server.docsDirectoryOrThrow()
        return listOf(
            FunctionParameterValueDiagnosticsSupplier(docsDirectory),
            FunctionUnresolvedParameterNameDiagnosticsSupplier(docsDirectory),
        )
    }
}
