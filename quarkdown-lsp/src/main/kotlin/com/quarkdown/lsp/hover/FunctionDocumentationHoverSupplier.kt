package com.quarkdown.lsp.hover

import com.quarkdown.lsp.documentation.extractContentAsMarkup
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.lsp.util.getByPatternContaining
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import java.io.File

/**
 * Provider of documentation on hover for function calls.
 * @property docsDirectory the directory containing the documentation files
 */
class FunctionDocumentationHoverSupplier(
    private val docsDirectory: File,
) : HoverSupplier {
    override fun getHover(
        params: HoverParams,
        text: String,
    ): Hover? {
        val function =
            params.position.getByPatternContaining(QuarkdownPatterns.FunctionCall.identifierInCall, text)
                ?: return null

        val documentation =
            DokkaHtmlWalker(docsDirectory)
                .walk()
                .find { it.name == function }
                ?.extractor()
                ?: return null

        return Hover(documentation.extractContentAsMarkup())
    }
}
