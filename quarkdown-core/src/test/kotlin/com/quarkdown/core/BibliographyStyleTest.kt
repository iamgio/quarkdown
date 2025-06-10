package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyStyle
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for bibliography styles.
 */
class BibliographyStyleTest {
    private val article =
        ArticleBibliographyEntry(
            citationKey = "einstein",
            title = "Zur Elektrodynamik bewegter Körper. (German) [On the electrodynamics of moving bodies]",
            author = "Einstein, Albert",
            year = "1905",
            journal = "Annalen der Physik",
            volume = "322",
            number = "10",
            pages = "891--921",
            month = null,
            doi = "http://dx.doi.org/10.1002/andp.19053221004",
            publisher = null,
        )

    @Test
    fun plain() {
        val style = BibliographyStyle.Plain
        val label = style.labelProvider.getLabel(article, 0)
        val content = article.accept(style.contentProvider)
        assertEquals("[1]", label)
        assertNodeEquals(
            buildInline {
                text("Einstein, Albert")
                text(". ")
                text("Zur Elektrodynamik bewegter Körper. (German) [On the electrodynamics of moving bodies]")
                text(". ")
                emphasis { text("Annalen der Physik") }
                text(", ")
                text("322")
                text("(10)")
                text(":891--921")
                text(", ")
                text("1905")
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }
}
