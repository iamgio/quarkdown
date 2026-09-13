package com.quarkdown.lsp.documentation

import com.quarkdown.core.parser.walker.funcall.lastChainedCall
import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.tokenizer.FunctionCall

/**
 * Retrieves the documentation for a function in the specified documentation directory.
 * @param docs the source of the documentation index
 * @param name name of the function to look up
 * @return the [DocumentedFunction] if found
 */
fun getDocumentation(
    docs: DocsIndexSource,
    name: String,
): DocumentedFunction? =
    CacheableFunctionCatalogue
        .getCatalogue(docs)
        .find { it.name == name }

/**
 * Retrieves the documentation for a function call in the specified documentation directory.
 * @param docs the source of the documentation index
 * @return the [DocumentedFunction] if found
 */
fun FunctionCall.getDocumentation(docs: DocsIndexSource): DocumentedFunction? =
    getDocumentation(docs, this.parserResult.value.lastChainedCall.name)
