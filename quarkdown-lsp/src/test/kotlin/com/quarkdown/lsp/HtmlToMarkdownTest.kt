package com.quarkdown.lsp

import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.lsp.documentation.HtmlToMarkdown
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for HTML-to-Markdown conversion for LSP documentation.
 */
class HtmlToMarkdownTest {
    @Test
    fun `links in code blocks are flattened to plain text`() {
        val html = """<pre><code class="lang-kotlin">.func {<a href="type.html">Type</a>}</code></pre>"""
        assertEquals(
            "```lang-kotlin\n.func {Type}\n```",
            HtmlToMarkdown.convert(html).trim(),
        )
    }

    @Test
    fun `stdlib page`() {
        val html = javaClass.getResourceAsStream("/html-to-markdown/align.html")!!.bufferedReader().use { it.readText() }
        val md = javaClass.getResourceAsStream("/html-to-markdown/align.md")!!.bufferedReader().use { it.readText() }

        assertEquals(
            md.normalizeLineSeparators(),
            HtmlToMarkdown.convert(html).normalizeLineSeparators(),
        )
    }
}
