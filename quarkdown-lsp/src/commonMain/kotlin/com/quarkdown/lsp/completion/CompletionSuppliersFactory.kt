package com.quarkdown.lsp.completion

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
     * @param docs the source of the documentation index
     * @return the default list of [CompletionSupplier] instances
     */
    fun default(docs: DocsIndexSource): List<CompletionSupplier> = this.functions(docs)

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
