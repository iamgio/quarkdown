package com.quarkdown.core

import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.bibtex.BibTeXBibliographyParser
import com.quarkdown.core.bibliography.structuredAuthors
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for .bib bibliographies.
 */
class BibTeXBibliographyTest {
    // https://github.com/plk/biblatex/blob/dev/bibtex/bib/biblatex/biblatex-examples.bib

    private fun getBibliography(resourceName: String) =
        BibTeXBibliographyParser.parse(javaClass.getResourceAsStream("/bib/$resourceName.bib")!!.reader())

    @Test
    fun article() {
        val entry = getBibliography("article").entries.single()
        assertEquals(
            ArticleBibliographyEntry(
                citationKey = "angenendt",
                title = "In Honore Salvatoris -- Vom Sinn und Unsinn der Patrozinienkunde",
                author = "Angenendt, Arnold",
                year = "2002",
                journal = "Revue d'Histoire Ecclésiastique",
                volume = "97",
                number = null,
                pages = "431--456, 791--823",
                month = null,
                doi = null,
                publisher = "Institut de Recherches Historiques du Septentrion",
                extraFields =
                    mapOf(
                        "langid" to "german",
                        "indextitle" to "In Honore Salvatoris",
                        "shorttitle" to "In Honore Salvatoris",
                        "annotation" to "A German article in a French journal. " +
                            "Apart from that, a typical \\texttt{article} entry. Note the \\texttt{indextitle} field",
                    ),
            ),
            entry,
        )
        assertEquals(
            BibliographyEntryAuthor(
                fullName = "Angenendt, Arnold",
                firstName = "Arnold",
                lastName = "Angenendt",
            ),
            entry.structuredAuthors.single(),
        )
    }

    @Test
    fun book() {
        val entry = getBibliography("book").entries.single()
        assertEquals(
            BookBibliographyEntry(
                citationKey = "averroes/hannes",
                title = "Des Averroes Abhandlung: Uber die Moglichkeit der Conjunktion oder Uber den materiellen Intellekt",
                author = "Averroes",
                year = "1892",
                publisher = "C. A. Kaemmerer",
                editor = "Hannes, Ludwig",
                volume = "1",
                series = null,
                address = "Halle an der Saale",
                edition = "1",
                extraFields =
                    mapOf(
                        "translator" to "Hannes, Ludwig",
                        "annotator" to "Hannes, Ludwig",
                        "keywords" to "primary",
                        "langid" to "german",
                        "sorttitle" to "Uber die Moglichkeit der Conjunktion",
                        "indexsorttitle" to "Uber die Moglichkeit der Conjunktion",
                        "indextitle" to "Über die Möglichkeit der Conjunktion",
                        "annotation" to "An annotated edition",
                    ),
            ),
            entry,
        )
        assertEquals(
            BibliographyEntryAuthor(
                fullName = "Averroes",
                firstName = null,
                lastName = "Averroes",
            ),
            entry.structuredAuthors.single(),
        )
    }

    @Test
    fun `generic (online)`() {
        val entry = getBibliography("online").entries.single()
        assertEquals(
            GenericBibliographyEntry(
                citationKey = "baez/online",
                title = "Higher-Dimensional Algebra {V}: 2-Groups",
                author = "Baez, John C. and Lauda, Aaron D.",
                year = "2004",
                url = "https://arxiv.org/abs/math/0307200v3",
                extraFields =
                    mapOf(
                        "version" to "3",
                        "langid" to "english",
                        "langidopts" to "variant=american",
                        "eprinttype" to "arxiv",
                        "eprint" to "math/0307200v3",
                        "annotation" to "An online reference from arXiv",
                    ),
            ),
            entry,
        )
        assertEquals(
            listOf(
                BibliographyEntryAuthor(
                    fullName = "Baez, John C.",
                    firstName = "John C.",
                    lastName = "Baez",
                ),
                BibliographyEntryAuthor(
                    fullName = "Lauda, Aaron D.",
                    firstName = "Aaron D.",
                    lastName = "Lauda",
                ),
            ),
            entry.structuredAuthors,
        )
    }

    @Test
    fun `generic (misc)`() {
        val entry = getBibliography("misc").entries.single()
        assertEquals(
            GenericBibliographyEntry(
                citationKey = "knuthwebsite",
                title = "Knuth: Computers and Typesetting",
                author = "Donald Knuth",
                year = null,
                url = "http://www-cs-faculty.stanford.edu/~uno/abcde.html",
                extraFields = emptyMap(),
            ),
            entry,
        )
        assertEquals(
            listOf(
                BibliographyEntryAuthor(
                    fullName = "Donald Knuth",
                    firstName = "Donald",
                    lastName = "Knuth",
                ),
            ),
            entry.structuredAuthors,
        )
    }
}
