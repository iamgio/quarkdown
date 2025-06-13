package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

private const val BIBLIOGRAPHY_CALL = ".noautopagebreak\n.bibliography {bib/bibliography.bib}"

private const val PLAIN_BIBLIOGRAPHY_OUTPUT =
    "<div class=\"bibliography bibliography-plain\">" +
        "<span class=\"bibliography-entry-label\">[1]</span>" +
        "<span class=\"bibliography-entry-content\">" +
        "Albert Einstein. " +
        "Zur Elektrodynamik bewegter Körper. (German) [On the electrodynamics of moving bodies]. " +
        "<em>Annalen der Physik</em>" +
        ", " +
        "322(10):891--921, 1905." +
        "</span>" +
        "<span class=\"bibliography-entry-label\">[2]</span>" +
        "<span class=\"bibliography-entry-content\">" +
        "Michel Goossens, Frank Mittelbach, and Alexander Samarin. " +
        "<em>The LaTeX Companion</em>" +
        ". " +
        "Addison-Wesley, Reading, Massachusetts, 1993." +
        "</span>" +
        "<span class=\"bibliography-entry-label\">[3]</span>" +
        "<span class=\"bibliography-entry-content\">" +
        "Donald Knuth. " +
        "Knuth: Computers and Typesetting. " +
        "<a href=\"http://www-cs-faculty.stanford.edu/~uno/abcde.html\">" +
        "http://www-cs-faculty.stanford.edu/~uno/abcde.html" +
        "</a>" +
        "." +
        "</span>" +
        "</div>"

/**
 * Tests for bibliographies and citations.
 */
class BibliographyTest {
    @Test
    fun `bibliography from bib file`() {
        execute(BIBLIOGRAPHY_CALL) {
            assertEquals(
                PLAIN_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }

    @Test
    fun `localized bibliography title`() {
        execute(".doclang {en}\n$BIBLIOGRAPHY_CALL") {
            assertEquals(
                "<h1>" +
                    "References" +
                    "</h1>" +
                    PLAIN_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }

    @Test
    fun `custom bibliography title`() {
        execute("$BIBLIOGRAPHY_CALL title:{My bibliography}") {
            assertEquals(
                "<h1>" +
                    "My bibliography" +
                    "</h1>" +
                    PLAIN_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }

    @Test
    fun citation() {
        execute(
            "abc .cite {einstein} def .cite {latexcompanion} ghi .cite {knuthwebsite}\n\n" +
                BIBLIOGRAPHY_CALL +
                "\nabc .cite {einstein} def .cite {latexcompanion} ghi .cite {knuthwebsite}\n\n",
        ) {
            assertEquals(
                "<p>abc [1] def [2] ghi [3]</p>" +
                    PLAIN_BIBLIOGRAPHY_OUTPUT +
                    "<p>abc [1] def [2] ghi [3]</p>",
                it,
            )
        }
    }

    @Test
    fun `unresolved citation`() {
        execute(
            "abc .cite {abc}\n\n" +
                BIBLIOGRAPHY_CALL,
        ) {
            assertEquals(
                "<p>abc [???]</p>" +
                    PLAIN_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }
}
