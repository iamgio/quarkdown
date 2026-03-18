package com.quarkdown.core

import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.bibliography.style.csl.CslBibliographyStyle
import com.quarkdown.core.util.node.toPlainText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for CSL-based bibliography styles via [CslBibliographyStyle].
 */
class CslBibliographyStyleTest {
    private fun bibResource(name: String) = javaClass.getResourceAsStream("/bib/$name")!!

    private fun cslStyle(styleName: String): CslBibliographyStyle =
        CslBibliographyStyle.from(styleName, bibResource("bibliography.bib"), "bibliography.bib")

    @Test
    fun `csl apa, article citation label`() {
        val style = cslStyle("apa")
        val entry = style.bibliography.entries["einstein"]!!
        val label = style.labelProvider.getCitationLabel(listOf(entry))
        assertTrue("Einstein" in label, "APA citation label should contain author name, got: $label")
        assertTrue("1905" in label, "APA citation label should contain year, got: $label")
    }

    @Test
    fun `csl apa, article content contains formatted nodes`() {
        val style = cslStyle("apa")
        val entry = style.bibliography.entries["einstein"]!!
        val content = style.contentOf(entry)
        val plainText = content.toPlainText()

        // APA article: Author (Year). Title. *Journal*, *Volume*(Issue), Pages. DOI
        assertTrue("Einstein" in plainText, "Should contain author: $plainText")
        assertTrue("1905" in plainText, "Should contain year: $plainText")
        assertTrue("Annalen" in plainText, "Should contain journal: $plainText")

        // Journal name should be emphasized (italic).
        val hasEmphasizedJournal = content.any { node -> node is Emphasis && node.children.toPlainText().contains("Annalen") }
        assertTrue(hasEmphasizedJournal, "Journal name should be emphasized in APA: $content")

        // DOI should be a link.
        val hasDoiLink = content.any { node -> node is Link && "doi" in node.url.lowercase() }
        assertTrue(hasDoiLink, "DOI should be rendered as a link in APA: $content")
    }

    @Test
    fun `csl apa, book content`() {
        val style = cslStyle("apa")
        val entry = style.bibliography.entries["latexcompanion"]!!
        val content = style.contentOf(entry)
        val plainText = content.toPlainText()

        assertTrue("Goossens" in plainText, "Should contain author: $plainText")
        assertTrue("LaTeX Companion" in plainText || "latex companion" in plainText.lowercase(), "Should contain title: $plainText")

        // Book title should be emphasized in APA.
        val hasEmphasizedTitle =
            content.any { node ->
                node is Emphasis &&
                    node.children
                        .toPlainText()
                        .lowercase()
                        .contains("latex companion")
            }
        assertTrue(hasEmphasizedTitle, "Book title should be emphasized in APA: $content")
    }

    @Test
    fun `csl apa, misc content`() {
        val style = cslStyle("apa")
        val entry = style.bibliography.entries["knuthwebsite"]!!
        val content = style.contentOf(entry)
        val plainText = content.toPlainText()
        assertTrue("Knuth" in plainText, "Should contain author: $plainText")
    }

    @Test
    fun `csl ieee, article citation label`() {
        val style = cslStyle("ieee")
        val entry = style.bibliography.entries["einstein"]!!
        val label = style.labelProvider.getCitationLabel(listOf(entry))
        assertTrue("[" in label && "]" in label, "IEEE citation label should be bracketed, got: $label")
    }

    @Test
    fun `csl ieee, article content`() {
        val style = cslStyle("ieee")
        val entry = style.bibliography.entries["einstein"]!!
        val content = style.contentOf(entry)
        val plainText = content.toPlainText()

        assertTrue("Einstein" in plainText, "Should contain author: $plainText")
        assertTrue("1905" in plainText, "Should contain year: $plainText")
    }

    @Test
    fun `csl style name`() {
        val style = cslStyle("apa")
        assertEquals("apa", style.name)
    }

    @Test
    fun `csl ieee, list label is numbered`() {
        val style = cslStyle("ieee")
        val entry = style.bibliography.entries["einstein"]!!
        val label = style.labelProvider.getListLabel(entry, 0)
        assertTrue("[" in label && "]" in label, "IEEE list label should be numbered, got: $label")
    }

    @Test
    fun `csl apa, list label is empty`() {
        val style = cslStyle("apa")
        val entry = style.bibliography.entries["einstein"]!!
        assertEquals("", style.labelProvider.getListLabel(entry, 0))
    }
}
