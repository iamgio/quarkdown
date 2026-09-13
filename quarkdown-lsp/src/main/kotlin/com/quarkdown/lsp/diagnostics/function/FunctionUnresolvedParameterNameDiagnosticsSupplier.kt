package com.quarkdown.lsp.diagnostics.function

import com.quarkdown.lsp.diagnostics.AbstractFunctionCallDiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.SimpleDiagnostic
import com.quarkdown.lsp.diagnostics.cause.DiagnosticCause
import com.quarkdown.lsp.diagnostics.cause.UnresolvedParameterNameDiagnosticCause
import com.quarkdown.lsp.documentation.DocsIndexSource
import com.quarkdown.lsp.documentation.getDocumentation
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.quarkdoc.reader.DocsFunction

/**
 * A diagnostics supplier that checks the existence of function parameter names that named arguments refer to.
 * @param docs the source of the documentation index
 */
class FunctionUnresolvedParameterNameDiagnosticsSupplier(
    private val docs: DocsIndexSource,
) : AbstractFunctionCallDiagnosticsSupplier() {
    override fun getDiagnostics(
        functionName: String,
        tokens: List<FunctionCallToken>,
        call: FunctionCall,
    ): List<SimpleDiagnostic> {
        val function = getDocumentation(this.docs, functionName) ?: return emptyList()

        return tokens
            .asSequence()
            .filter { it.type == FunctionCallToken.Type.PARAMETER_NAME }
            .mapNotNull { token ->
                val parameterName = token.lexeme.trim()
                validate(function.data, parameterName)?.let { cause ->
                    SimpleDiagnostic(token.range, cause)
                }
            }.toList()
    }

    /**
     * Validates a parameter name against a function to extract any diagnostics about unresolved parameter names.
     * @param function the function to validate against
     * @param parameterName the parameter name to validate
     * @return a [DiagnosticCause] if the parameter name is unresolved, `null`
     */
    private fun validate(
        function: DocsFunction,
        parameterName: String,
    ): DiagnosticCause? =
        when {
            // If there are allowed values, checks if the value is among them.
            function.parameters.none { it.name == parameterName } ->
                UnresolvedParameterNameDiagnosticCause(function, parameterName)

            else -> null
        }
}
