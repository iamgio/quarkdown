package com.quarkdown.quarkdoc.dokka

import com.quarkdown.quarkdoc.dokka.index.HtmlToMarkdown
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for HTML-to-Markdown conversion of documentation content.
 */
class HtmlToMarkdownTest {
    private fun String.normalizeLineSeparators(): String = replace(Regex("\\R"), "\n")

    @Test
    fun `links in code blocks are flattened to plain text`() {
        val html = """<pre><code class="lang-kotlin">.func {<a href="type.html">Type</a>}</code></pre>"""
        assertEquals(
            "```lang-kotlin\n.func {Type}\n```",
            HtmlToMarkdown.convert(html).trim(),
        )
    }

    private fun assertPageConversion(name: String) {
        val html = javaClass.getResourceAsStream("/html-to-markdown/$name.html")!!.bufferedReader().use { it.readText() }
        val md = javaClass.getResourceAsStream("/html-to-markdown/$name.md")!!.bufferedReader().use { it.readText() }

        assertEquals(
            md.normalizeLineSeparators(),
            HtmlToMarkdown.convert(html).normalizeLineSeparators(),
        )
    }

    @Test
    fun `stdlib page`() = assertPageConversion("align")

    @Test
    fun `stdlib page with wrapped signature keeps line breaks`() = assertPageConversion("getat")
}
