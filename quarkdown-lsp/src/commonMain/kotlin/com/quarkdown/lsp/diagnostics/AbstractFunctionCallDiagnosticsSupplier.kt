package com.quarkdown.lsp.diagnostics

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.cache.functionCalls
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.util.tokensByChainedCall

/**
 * A [DiagnosticsSupplier] that provides diagnostics for function calls.
 *
 * In chained calls, diagnostics are provided for each function in the chain.
 */
abstract class AbstractFunctionCallDiagnosticsSupplier : DiagnosticsSupplier {
    override fun getDiagnostics(document: TextDocument): List<SimpleDiagnostic> = document.functionCalls.flatMap(::getDiagnostics)

    private fun getDiagnostics(call: FunctionCall): List<SimpleDiagnostic> =
        call.tokensByChainedCall
            .flatMap { (functionName, tokens) -> getDiagnostics(functionName, tokens, call) }
            .toList()

    /**
     * Provides diagnostics for a function call.
     *
     * If the function is part of a chained call, [functionName] is not granted to be the same as the name of [call].
     * @param functionName the name of the function to provide diagnostics for
     * @param tokens the tokens of the function call
     * @param call the full function call
     * @return a list of diagnostics for the function call
     */
    protected abstract fun getDiagnostics(
        functionName: String,
        tokens: List<FunctionCallToken>,
        call: FunctionCall,
    ): List<SimpleDiagnostic>
}
