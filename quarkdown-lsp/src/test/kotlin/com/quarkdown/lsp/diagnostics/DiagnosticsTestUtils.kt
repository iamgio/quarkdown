package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.TextDocument
import java.io.File

/**
 * Constants and utility methods for diagnostics tests.
 */
object DiagnosticsTestUtils {
    const val ALIGN_FUNCTION = "align"
    const val ALIGNMENT_PARAMETER = "alignment"

    val DOCS_DIRECTORY = File("src/test/resources/docs")

    /**
     * Gets diagnostics from a supplier for the given text.
     */
    fun getDiagnostics(
        text: String,
        supplier: DiagnosticsSupplier,
    ): List<SimpleDiagnostic> {
        val document = TextDocument(text = text)
        return supplier.getDiagnostics(document)
    }
}
