package com.quarkdown.lsp.cache

import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsWalker

/**
 * Cached information extracted from the Quarkdown documentation about a function.
 * @param data the processed function data
 * @param rawData the raw data from the documentation walker
 * @param documentationMarkdown the documentation content in Markdown, if available
 */
data class DocumentedFunction(
    val data: DocsFunction,
    val rawData: DocsWalker.Result<*>,
    val documentationMarkdown: String?,
) {
    /**
     * The name of the function.
     */
    val name: String
        get() = data.name
}
