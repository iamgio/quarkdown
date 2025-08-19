package com.quarkdown.lsp.documentation

import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.quarkdoc.reader.DocsWalker
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlContentExtractor
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import java.io.File

fun FunctionCall.findDocumentation(docsDirectory: File): DocsWalker.Result<DokkaHtmlContentExtractor>? {
    val functionName = this.parserResult.value.name

    return DokkaHtmlWalker(docsDirectory)
        .walk()
        .filter { it.isInModule }
        .find { it.name == functionName }
}
