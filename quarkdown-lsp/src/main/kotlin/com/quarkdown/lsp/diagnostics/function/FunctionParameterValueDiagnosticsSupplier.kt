package com.quarkdown.lsp.diagnostics.function

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.SimpleDiagnostic
import com.quarkdown.lsp.diagnostics.cause.DiagnosticCause
import com.quarkdown.lsp.diagnostics.cause.UnallowedValueDiagnosticCause
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.util.getParameterAtSourceIndex
import com.quarkdown.quarkdoc.reader.DocsParameter
import java.io.File

/**
 * A diagnostics supplier that checks function parameter values against their allowed values as specified in the documentation.
 * @param docsDirectory the directory where function documentation files are stored
 */
class FunctionParameterValueDiagnosticsSupplier(
    private val docsDirectory: File,
) : DiagnosticsSupplier {
    override fun getDiagnostics(document: TextDocument): List<SimpleDiagnostic> = document.functionCalls.flatMap(::getDiagnostics)

    private fun getDiagnostics(call: FunctionCall): List<SimpleDiagnostic> {
        val documentation = call.getDocumentation(this.docsDirectory) ?: return emptyList()
        val valueTokens = call.tokens.filter { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_VALUE }
        val diagnostics = mutableListOf<SimpleDiagnostic>()

        // Validating each value to its corresponding parameter.
        valueTokens.forEach { token ->
            // Getting the parameter corresponding to this value.
            val parameter: DocsParameter =
                call.getParameterAtSourceIndex(documentation.data, token.range.start) ?: return@forEach

            // Validating the value against the parameter.
            validate(parameter, token.lexeme)?.let {
                diagnostics += SimpleDiagnostic(token.range, it)
            }
        }

        return diagnostics
    }

    /**
     * Validates a value against a parameter to extract any diagnostics.
     * @param parameter the parameter to validate against
     * @param value the value to validate
     * @return a [DiagnosticCause] if the value is invalid, `null` otherwise
     */
    private fun validate(
        parameter: DocsParameter,
        value: String,
    ): DiagnosticCause? =
        when {
            parameter.allowedValues?.let { value in it } == false ->
                UnallowedValueDiagnosticCause(parameter, value)

            else -> null
        }
}
