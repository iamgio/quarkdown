package com.quarkdown.quarkdoc.dokka.index

import com.fleeksoft.ksoup.Ksoup
import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.quarkdown.quarkdoc.reader.DocsFunction

/**
 * Extractor of the main content of Dokka-generated HTML files,
 * used by [DocsIndexWriterPostAction] to pre-extract each function's documentation.
 */
class DokkaHtmlContentExtractor(
    private val html: String,
) : DocsContentExtractor {
    override fun extractContent(): String? =
        Ksoup
            .parse(html)
            .selectFirst("#main .content")
            ?.apply {
                // Removes copy buttons from code blocks.
                select(".copy-tooltip").remove()
            }?.outerHtml()

    override fun extractFunctionData(): DocsFunction? = null
}
