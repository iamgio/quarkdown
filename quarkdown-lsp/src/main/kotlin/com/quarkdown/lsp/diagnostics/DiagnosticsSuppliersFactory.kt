package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.QuarkdownLanguageServer
import com.quarkdown.lsp.diagnostics.function.FunctionParameterValueDiagnosticsSupplier

/**
 * Factory for creating a list of [DiagnosticsSupplier]s.
 */
object DiagnosticsSuppliersFactory {
    /**
     * @param server the Quarkdown language server instance
     * @return the default list of [DiagnosticsSuppliersFactory] instances
     */
    fun default(server: QuarkdownLanguageServer) =
        listOf(
            FunctionParameterValueDiagnosticsSupplier(
                docsDirectory = server.docsDirectoryOrThrow(),
            ),
        )
}
