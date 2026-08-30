package com.quarkdown.quarkdoc.dokka.index

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.TextNode
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter

/**
 * Helper to convert documentation HTML to Markdown.
 *
 * The HTML is preprocessed with ksoup into a simplified, structurally valid form,
 * then serialized and handed to the Markdown converter.
 */
object HtmlToMarkdown {
    private val converter = FlexmarkHtmlConverter.builder().build()

    /**
     * Converts HTML to Markdown.
     * @param html the HTML string to convert
     * @return the converted Markdown string
     */
    fun convert(html: String): String {
        val document =
            Ksoup
                .parse(html)
                .apply {
                    // Flattens links and spans in code blocks to plain text: a fence must not carry Markdown links.
                    // Line breaks, which Dokka renders as <br> elements (e.g. in wrapped signatures), are preserved.
                    select("pre code").forEach { code ->
                        code.select("br").forEach { it.replaceWith(TextNode("\n")) }
                        code.text(code.wholeText())
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
