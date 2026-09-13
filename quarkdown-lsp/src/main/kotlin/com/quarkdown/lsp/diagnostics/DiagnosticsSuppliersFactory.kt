package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.QuarkdownLanguageServer
import com.quarkdown.lsp.diagnostics.function.FunctionDuplicateParameterNameDiagnosticsSupplier
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
        val docs = server.docsIndexSourceOrThrow()
        return listOf(
            FunctionParameterValueDiagnosticsSupplier(docs),
            FunctionUnresolvedParameterNameDiagnosticsSupplier(docs),
            FunctionDuplicateParameterNameDiagnosticsSupplier(),
        )
    }
}
