package com.quarkdown.lsp.completion

import com.quarkdown.lsp.QuarkdownLanguageServer
import com.quarkdown.lsp.completion.function.name.FunctionNameCompletionSupplier
import com.quarkdown.lsp.completion.function.parameter.FunctionParameterAllowedValuesCompletionSupplier
import com.quarkdown.lsp.completion.function.parameter.FunctionParameterNameCompletionSupplier
import com.quarkdown.lsp.documentation.DocsIndexSource

/**
 * Factory for creating a list of [CompletionSupplier]s.
 */
object CompletionSuppliersFactory {
    /**
     * The default completion suppliers cover completions for:
     * - Function name ([FunctionNameCompletionSupplier]), both regular and chained
     * - Function parameter name ([FunctionParameterNameCompletionSupplier])
     * - Function parameter values ([FunctionParameterAllowedValuesCompletionSupplier])
     *
     * @param server the Quarkdown language server instance
     * @return the default list of [CompletionSupplier] instances
     */
    fun default(server: QuarkdownLanguageServer): List<CompletionSupplier> = this.functions(docs = server.docsIndexSourceOrThrow())

    /**
     * @param docs the source of the documentation index
     * @return the [CompletionSupplier]s that handle function call completions
     */
    internal fun functions(docs: DocsIndexSource): List<CompletionSupplier> =
        listOf(
            FunctionNameCompletionSupplier(docs),
            FunctionParameterAllowedValuesCompletionSupplier(docs),
            FunctionParameterNameCompletionSupplier(docs),
        )
}
