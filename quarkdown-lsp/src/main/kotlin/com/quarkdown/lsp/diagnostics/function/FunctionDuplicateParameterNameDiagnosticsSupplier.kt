package com.quarkdown.lsp.diagnostics.function

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.diagnostics.DiagnosticsSupplier
import com.quarkdown.lsp.diagnostics.SimpleDiagnostic
import com.quarkdown.lsp.diagnostics.cause.DuplicateParameterNameDiagnosticCause
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.util.tokensByChainedCall

/**
 * A diagnostics supplier that checks for duplicate function parameter names in function calls.
 */
class FunctionDuplicateParameterNameDiagnosticsSupplier : DiagnosticsSupplier {
    override fun getDiagnostics(document: TextDocument): List<SimpleDiagnostic> = document.functionCalls.flatMap(::getDiagnostics)

    private fun getDiagnostics(call: FunctionCall): List<SimpleDiagnostic> =
        call.tokensByChainedCall
            .flatMap { (_, tokens) -> validate(tokens) }
            .toList()

    private fun validate(tokens: List<FunctionCallToken>): List<SimpleDiagnostic> =
        tokens
            .asSequence()
            .filter { it.type == FunctionCallToken.Type.PARAMETER_NAME }
            .groupBy { it.lexeme.trim() }
            .filter { (_, tokens) -> tokens.size > 1 }
            .flatMap { (parameterName, tokens) ->
                tokens.map { token ->
                    SimpleDiagnostic(token.range, DuplicateParameterNameDiagnosticCause(parameterName))
                }
            }.toList()
}
