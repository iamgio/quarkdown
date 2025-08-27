package com.quarkdown.lsp.documentation

import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.jsoup.Jsoup

/**
 * Helper to convert HTML to Markdown, suitable for use in LSP documentation.
 */
object HtmlToMarkdown {
    /**
     * Converts HTML to Markdown, suitable for use in LSP documentation.
     * @param html the HTML string to convert
     * @return the converted Markdown string
     */
    fun convert(html: String): String {
        val processedHtml =
            Jsoup
                .parse(html)
                .apply {
                    // Cleans up links in code blocks.
                    select("pre code").forEach {
                        it.text(it.wholeText())
                    }

                    select(".table").forEach {
                        it.tagName("ul")
                    }
                    select(".main-subrow").forEach {
                        it.tagName("li")
                    }

                    select(".table h4").forEach {
                        it.tagName("p")
                    }

                    select(".main-subrow > *").forEach {
                        it.tagName("p")
                    }

                    select("u").forEach {
                        it.tagName("strong")
                    }
                }
        return FlexmarkHtmlConverter.builder().build().convert(processedHtml)
    }
}

/**
 * @return [this] HTML content converted to [MarkupContent]
 */
fun String.htmlToMarkup(): MarkupContent? = MarkupContent(MarkupKind.MARKDOWN, HtmlToMarkdown.convert(this))

/**
 * @return the content extracted from the documentation as [MarkupContent], or `null` if no content is available
 */
fun DocsContentExtractor.extractContentAsMarkup(): MarkupContent? = extractContent()?.htmlToMarkup()
