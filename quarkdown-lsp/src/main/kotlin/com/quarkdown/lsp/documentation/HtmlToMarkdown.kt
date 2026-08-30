package com.quarkdown.lsp.documentation

import com.fleeksoft.ksoup.Ksoup
import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind

/**
 * Helper to convert HTML to Markdown, suitable for use in LSP documentation.
 *
 * The HTML is preprocessed with ksoup into a simplified, structurally valid form,
 * then serialized and handed to the Markdown converter.
 */
object HtmlToMarkdown {
    private val converter = FlexmarkHtmlConverter.builder().build()

    /**
     * Converts HTML to Markdown, suitable for use in LSP documentation.
     * @param html the HTML string to convert
     * @return the converted Markdown string
     */
    fun convert(html: String): String {
        val document =
            Ksoup
                .parse(html)
                .apply {
                    // Flattens links and spans in code blocks to plain text: a fence must not carry Markdown links.
                    // Dokka renders signatures as per-token spans split across formatted source lines,
                    // whose line breaks are formatting artifacts to collapse; plain code samples keep theirs.
                    select("pre code").forEach {
                        val isTokenized = it.select("span.token").isNotEmpty()
                        val text = it.wholeText()
                        it.text(if (isTokenized) text.replace(Regex("\\s+"), " ").trim() else text)
                    }

                    // Parameter names are underlined in the source, but bold reads better in tooltips.
                    select("u").forEach {
                        it.tagName("strong")
                    }

                    // Parameter tables become lists.
                    select(".table").forEach {
                        it.tagName("ul")
                    }
                    select(".main-subrow").forEach {
                        it.tagName("li")
                    }
                    select(".main-subrow h4").forEach {
                        it.tagName("p")
                    }

                    // Wrappers dissolve so the serialized HTML remains structurally valid:
                    // block elements inside `p`, or between `ul` and `li`, would be relocated
                    // when the converter reparses the serialized document.
                    select(".table-row").forEach {
                        it.unwrap()
                    }
                    select(".main-subrow div, .main-subrow span").forEach {
                        it.unwrap()
                    }

                    // Compact serialization: pretty-printing would inject whitespace between elements.
                    outputSettings().prettyPrint(false)
                }
        return converter.convert(document.body().html())
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
