package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.diagnostics.function.FunctionDuplicateParameterNameDiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.function.FunctionParameterValueDiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.function.FunctionUnresolvedParameterNameDiagnosticsSupplier
import com.quarkdown.lsp.documentation.DocsIndexSource

/**
 * Factory for creating a list of [DiagnosticsSupplier]s.
 */
object DiagnosticsSuppliersFactory {
    /**
     * @param docs the source of the documentation index
     * @return the default list of [DiagnosticsSupplier] instances
     */
    fun default(docs: DocsIndexSource): List<DiagnosticsSupplier> =
        listOf(
            FunctionParameterValueDiagnosticsSupplier(docs),
            FunctionUnresolvedParameterNameDiagnosticsSupplier(docs),
            FunctionDuplicateParameterNameDiagnosticsSupplier(),
        )
}
