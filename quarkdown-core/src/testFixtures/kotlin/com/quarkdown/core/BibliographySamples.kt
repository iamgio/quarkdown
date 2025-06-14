package com.quarkdown.core

import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry

/**
 * Sample [com.quarkdown.core.bibliography.Bibliography] entries for testing purposes.
 */
object BibliographySamples {
    val article =
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

    val book =
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

    val misc =
        GenericBibliographyEntry(
            citationKey = "knuthwebsite",
            title = "Knuth: Computers and Typesetting",
            author = "Donald Knuth",
            year = null,
            url = "http://www-cs-faculty.stanford.edu/~uno/abcde.html",
            extraFields = mapOf("version" to "1.0"),
        )
}
