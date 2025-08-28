package com.quarkdown.lsp.diagnostics.function

import com.quarkdown.lsp.diagnostics.AbstractFunctionCallDiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.SimpleDiagnostic
import com.quarkdown.lsp.diagnostics.cause.DiagnosticCause
import com.quarkdown.lsp.diagnostics.cause.UnallowedValueDiagnosticCause
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.pattern.QuarkdownPatterns
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
) : AbstractFunctionCallDiagnosticsSupplier() {
    override fun getDiagnostics(
        functionName: String,
        tokens: List<FunctionCallToken>,
        call: FunctionCall,
    ): List<SimpleDiagnostic> {
        val function = getDocumentation(this.docsDirectory, functionName) ?: return emptyList()
        val valueTokens = call.tokens.filter { it.type == FunctionCallToken.Type.INLINE_ARGUMENT_VALUE }
        val diagnostics = mutableListOf<SimpleDiagnostic>()

        // Validating each value to its corresponding parameter.
        valueTokens.forEach { token ->
            // Getting the parameter corresponding to this value.
            val parameter: DocsParameter =
                call.getParameterAtSourceIndex(function.data, token.range.start) ?: return@forEach

            // The value of the argument.
            val value = token.lexeme.trim()

            // Validating the value against the parameter.
            validate(parameter, value)?.let {
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
            // No diagnostics available if function calls are present in the value.
            QuarkdownPatterns.FunctionCall.identifierInCall in value ->
                null

            // If there are allowed values, checks if the value is among them.
            parameter.allowedValues?.let { value in it } == false ->
                UnallowedValueDiagnosticCause(parameter, value)

            else -> null
        }
}
