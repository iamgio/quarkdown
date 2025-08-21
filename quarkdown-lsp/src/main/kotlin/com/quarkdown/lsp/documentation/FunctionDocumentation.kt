package com.quarkdown.lsp.documentation

import com.quarkdown.core.parser.walker.funcall.lastChainedCall
import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.tokenizer.FunctionCall
import java.io.File

/**
 * Retrieves the documentation for a function call in the specified documentation directory.
 * @param docsDirectory the directory containing the documentation files
 * @param name optional name of the function to look up, which overrides the default one.
 * If `null`, uses the name of the last function call in the chain.
 * This is useful in case the function to be looked up is not the last one in the chain
 * @return the [DocumentedFunction] if found
 */
fun FunctionCall.getDocumentation(
    docsDirectory: File,
    name: String? = null,
): DocumentedFunction? {
    val functionName = name ?: this.parserResult.value.lastChainedCall.name

    return CacheableFunctionCatalogue
        .getCatalogue(docsDirectory)
        .find { it.name == functionName }
}
