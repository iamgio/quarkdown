package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.util.offsetToPosition
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Range

/**
 * A simple diagnostic that can be sent to the client.
 * @param range the range of the diagnostic in the document
 * @param message the message of the diagnostic
 * @param severity the severity of the diagnostic
 */
data class SimpleDiagnostic(
    val range: IntRange,
    val message: String,
    val severity: DiagnosticSeverity,
)

/**
 * Converts a [SimpleDiagnostic] to a [Diagnostic] suitable for the LSP to send to the client.
 * @param text the text of the document
 * @return the LSP diagnostic
 */
fun SimpleDiagnostic.toLspDiagnostic(text: String): Diagnostic =
    Diagnostic().also {
        it.range = Range(offsetToPosition(text, range.start), offsetToPosition(text, range.endInclusive))
        it.message = message
        it.severity = severity
    }
