package com.quarkdown.quarkdoc.reader

import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlContentExtractor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for extracting content from Dokka HTML files.
 */
class ContentExtractorTest {
    @Test
    fun `dokka html extractor`() {
        val subContentRange = 114..145
        val fullHtml = javaClass.getResourceAsStream("/content/lowercase.html")!!.bufferedReader().readText()
        assertEquals(
            fullHtml
                .lines()
                .subList(subContentRange.first, subContentRange.last)
                .joinToString("\n")
                .replace("\\s+".toRegex(), ""),
            DokkaHtmlContentExtractor(fullHtml)
                .extractContent()
                ?.replace("\\s+".toRegex(), ""),
        )
    }
}
