package com.quarkdown.core

import com.quarkdown.core.BibliographySamples.article
import com.quarkdown.core.BibliographySamples.book
import com.quarkdown.core.BibliographySamples.misc
import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.bibliography.style.getContent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for bibliography styles.
 */
class BibliographyStyleTest {
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
                text("(")
                text("10")
                text(")")
                text(":")
                text("891--921")
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
                text("1.0")
                text(". ")
                link("http://www-cs-faculty.stanford.edu/~uno/abcde.html") {
                    text("http://www-cs-faculty.stanford.edu/~uno/abcde.html")
                }
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
                text("“")
                text("Zur Elektrodynamik bewegter Körper. (German) [On the electrodynamics of moving bodies]")
                text("”")
                text(", ")
                emphasis { text("Annalen der Physik") }
                text(", ")
                text("vol. ")
                text("322")
                text(", ")
                text("no. ")
                text("10")
                text(", ")
                text("pp. ")
                text("891--921")
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
                text("“")
                text("Knuth: Computers and Typesetting")
                text("”")
                text(". ")
                text("1.0")
                text(". ")
                link("http://www-cs-faculty.stanford.edu/~uno/abcde.html") {
                    text("http://www-cs-faculty.stanford.edu/~uno/abcde.html")
                }
                text(".")
            }.let(::AstRoot),
            AstRoot(content),
        )
    }
}
