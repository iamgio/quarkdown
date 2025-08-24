package com.quarkdown.quarkdoc.reader.dokka

import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.anchors.Anchors
import com.quarkdown.quarkdoc.reader.anchors.getAnchorNextElement
import com.quarkdown.quarkdoc.reader.anchors.hasAnchor
import com.quarkdown.quarkdoc.reader.anchors.stripAnchors
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

private const val PARAMETERS_HEADER = "Parameters"

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
            ?.apply {
                // Removes copy buttons from code blocks.
                select(".top-right-position:has(.copy-icon)").remove()
            }?.outerHtml()

    override fun extractFunctionData(): DocsFunction? {
        val main =
            Jsoup
                .parse(html)
                .selectFirst("#main > .main-content")
                ?.takeIf { it.attr("data-page-type") == "member" }
                ?: return null

        return DocsFunction(
            name = main.selectFirst("h1")?.text() ?: "x",
            parameters = extractFunctionParameters(main),
            isLikelyChained = main.hasAnchor(Anchors.LIKELY_CHAINED),
        )
    }

    /**
     * Converts a row of the parameters table into a [DocsParameter].
     */
    private fun rowToParameter(row: Element): DocsParameter? {
        val name = row.children().firstOrNull()?.text() ?: return null
        val content = row.selectFirst(".title")

        return DocsParameter(
            name = name,
            description = content?.stripAnchors()?.html() ?: "",
            isOptional = row.hasAnchor(Anchors.OPTIONAL),
            isLikelyNamed = row.hasAnchor(Anchors.LIKELY_NAMED),
            isLikelyBody = row.hasAnchor(Anchors.LIKELY_BODY),
            allowedValues =
                row
                    .getAnchorNextElement(Anchors.VALUES)
                    ?.select("li")
                    ?.map { it.text() },
        )
    }

    private fun extractFunctionParameters(document: Element): List<DocsParameter> {
        val table =
            document
                .select("h4:contains($PARAMETERS_HEADER)")
                .firstOrNull()
                ?.nextElementSibling()
                ?: return emptyList()

        return table
            .getElementsByClass("main-subrow")
            .mapNotNull(::rowToParameter)
    }
}
