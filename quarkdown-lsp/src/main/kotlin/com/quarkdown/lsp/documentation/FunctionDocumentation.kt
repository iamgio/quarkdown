package com.quarkdown.lsp.documentation

import com.quarkdown.core.parser.walker.funcall.lastChainedCall
import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.tokenizer.FunctionCall
import java.io.File

/**
 * Retrieves the documentation for a function in the specified documentation directory.
 * @param docsDirectory the directory containing the documentation files
 * @param name name of the function to look up
 * @return the [DocumentedFunction] if found
 */
fun getDocumentation(
    docsDirectory: File,
    name: String,
): DocumentedFunction? =
    CacheableFunctionCatalogue
        .getCatalogue(docsDirectory)
        .find { it.name == name }

/**
 * Retrieves the documentation for a function call in the specified documentation directory.
 * @param docsDirectory the directory containing the documentation files
 * @return the [DocumentedFunction] if found
 */
fun FunctionCall.getDocumentation(docsDirectory: File): DocumentedFunction? =
    getDocumentation(docsDirectory, this.parserResult.value.lastChainedCall.name)
