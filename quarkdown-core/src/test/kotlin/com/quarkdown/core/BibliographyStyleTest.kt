package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.bibliography.style.getContent
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

    private val book =
        BookBibliographyEntry(
            citationKey = "latexcompanion",
            title = "The LaTeX Companion",
            author = "Michel Goossens and Frank Mittelbach and Alexander Samarin",
            year = "1993",
            publisher = "Addison-Wesley",
            editor = null,
            volume = null,
            series = null,
            address = "Reading, Massachusetts",
            edition = null,
        )

    private val misc =
        GenericBibliographyEntry(
            citationKey = "knuthwebsite",
            title = "Knuth: Computers and Typesetting",
            author = "Donald Knuth",
            year = null,
            extraFields = mapOf("url" to "http://www-cs-faculty.stanford.edu/~uno/abcde.html"),
        )

    @Test
    fun `plain, article`() {
        val style = BibliographyStyle.Plain
        val label = style.labelProvider.getLabel(article, 0)
        val content = style.contentProvider.getContent(article)
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

    @Test
    fun `plain, book`() {
        val style = BibliographyStyle.Plain
        val label = style.labelProvider.getLabel(book, 0)
        val content = style.contentProvider.getContent(book)
        assertEquals("[1]", label)
        assertNodeEquals(
            buildInline {
                text("Michel Goossens, Frank Mittelbach, and Alexander Samarin")
                text(". ")
                emphasis { text("The LaTeX Companion") }
                text(". ")
                text("Addison-Wesley")
                text(", ")
                text("Reading, Massachusetts")
                text(", ")
                text("1993")
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }

    @Test
    fun `plain, misc`() {
        val style = BibliographyStyle.Plain
        val content = style.contentProvider.getContent(misc)
        assertNodeEquals(
            buildInline {
                text("Donald Knuth")
                text(". ")
                text("Knuth: Computers and Typesetting")
                text(". ")
                text("http://www-cs-faculty.stanford.edu/~uno/abcde.html")
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }

    @Test
    fun `ieeetr, article`() {
        val style = BibliographyStyle.Ieeetr
        val label = style.labelProvider.getLabel(article, 0)
        val content = style.contentProvider.getContent(article)
        assertEquals("[1]", label)
        assertNodeEquals(
            buildInline {
                text("A. Einstein")
                text(", ")
                text("“Zur Elektrodynamik bewegter Körper. (German) [On the electrodynamics of moving bodies]”")
                text(", ")
                emphasis { text("Annalen der Physik") }
                text(", ")
                text("vol. 322")
                text(", ")
                text("no. 10")
                text(", ")
                text("pp. 891--921")
                text(", ")
                text("1905")
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }

    @Test
    fun `ieeetr, book`() {
        val style = BibliographyStyle.Ieeetr
        val content = style.contentProvider.getContent(book)
        assertNodeEquals(
            buildInline {
                text("M. Goossens, F. Mittelbach, and A. Samarin")
                text(", ")
                emphasis { text("The LaTeX Companion") }
                text(". ")
                text("Reading, Massachusetts")
                text(": ")
                text("Addison-Wesley")
                text(", ")
                text("1993")
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }

    @Test
    fun `ieeetr, misc`() {
        val style = BibliographyStyle.Ieeetr
        val content = style.contentProvider.getContent(misc)
        assertNodeEquals(
            buildInline {
                text("D. Knuth")
                text(", ")
                text("“Knuth: Computers and Typesetting”")
                text(". ")
                text("http://www-cs-faculty.stanford.edu/~uno/abcde.html")
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }
}
