package com.quarkdown.quarkdoc.reader.dokka

import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import org.jsoup.Jsoup

/**
 * Extractor of content from Dokka-generated HTML files.
 */
class DokkaHtmlContentExtractor(
    private val html: String,
) : DocsContentExtractor {
    override fun extractContent(): String? =
        Jsoup
            .parse(html)
            .selectFirst("#main .content")
            ?.outerHtml()
}
