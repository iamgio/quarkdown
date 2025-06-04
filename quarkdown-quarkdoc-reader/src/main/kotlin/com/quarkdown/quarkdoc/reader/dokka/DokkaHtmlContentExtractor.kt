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

    /**
     * @return a map of parameter names to their HTML descriptions
     */
    override fun extractParameters(): Map<String, String> {
        val table =
            Jsoup
                .parse(html)
                .select("h4:contains(Parameters)")
                .firstOrNull()
                ?.nextElementSibling()
                ?: return emptyMap()

        return table
            .getElementsByClass("main-subrow")
            .associate { row ->
                val name = row.children().firstOrNull()?.text() ?: ""
                val descriptionHtml = row.select(".title").html()
                name to descriptionHtml
            }
    }
}
