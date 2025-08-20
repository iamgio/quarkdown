package com.quarkdown.lsp.documentation

import com.quarkdown.lsp.cache.CacheableFunctionCatalogue
import com.quarkdown.lsp.cache.DocumentedFunction
import com.quarkdown.lsp.tokenizer.FunctionCall
import java.io.File

fun FunctionCall.getDocumentation(docsDirectory: File): DocumentedFunction? {
    val functionName = this.parserResult.value.name

    return CacheableFunctionCatalogue
        .getCatalogue(docsDirectory)
        .find { it.name == functionName }
}
