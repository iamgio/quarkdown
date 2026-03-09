package com.quarkdown.test

import com.quarkdown.rendering.plaintext.extension.plainText
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

private const val BIBLIOGRAPHY_CALL = ".bibliography {bib/bibliography.bib} decorativetitle:{yes}"

private const val IEEE_BIBLIOGRAPHY_OUTPUT =
    "<div class=\"bibliography bibliography-ieee\">" +
        "<span class=\"bibliography-entry-label\">[1]</span>" +
        "<span class=\"bibliography-entry-content\">" +
        "A. Einstein, \u201CZur Elektrodynamik bewegter K\u00F6rper. (German) " +
        "[On the electrodynamics of moving bodies],\u201D " +
        "<em>Annalen der Physik</em>" +
        ", vol. 322, Art. no. 10, 1905, doi: " +
        "<a href=\"http://dx.doi.org/10.1002/andp.19053221004\">" +
        "http://dx.doi.org/10.1002/andp.19053221004" +
        "</a>." +
        "</span>" +
        "<span class=\"bibliography-entry-label\">[2]</span>" +
        "<span class=\"bibliography-entry-content\">" +
        "M. Goossens, F. Mittelbach, and A. Samarin, " +
        "<em>The LaTeX Companion</em>" +
        ". Reading, Massachusetts: Addison-Wesley, 1993." +
        "</span>" +
        "<span class=\"bibliography-entry-label\">[3]</span>" +
        "<span class=\"bibliography-entry-content\">" +
        "D. Knuth, \u201CKnuth: Computers and Typesetting.\u201D [Online]. Available: " +
        "<a href=\"http://www-cs-faculty.stanford.edu/uno/abcde.html\">" +
        "http://www-cs-faculty.stanford.edu/uno/abcde.html" +
        "</a>" +
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
                IEEE_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }

    @Test
    fun `localized bibliography title`() {
        execute(".doclang {en}\n$BIBLIOGRAPHY_CALL") {
            assertEquals(
                "<h1 data-decorative=\"\">" +
                    "References" +
                    "</h1>" +
                    IEEE_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }

    @Test
    fun `custom bibliography title`() {
        execute("$BIBLIOGRAPHY_CALL title:{My bibliography}") {
            assertEquals(
                "<h1 data-decorative=\"\">" +
                    "My bibliography" +
                    "</h1>" +
                    IEEE_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }

    @Test
    fun citation() {
        execute(
            """
            abc .cite {einstein} def .cite {latexcompanion} ghi .cite {knuthwebsite}

            $BIBLIOGRAPHY_CALL

            abc .cite {einstein} def .cite {latexcompanion} ghi .cite {knuthwebsite}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>abc [1] def [2] ghi [3]</p>" +
                    IEEE_BIBLIOGRAPHY_OUTPUT +
                    "<p>abc [1] def [2] ghi [3]</p>",
                it,
            )
        }
    }

    @Test
    fun `citation (plaintext)`() {
        execute(
            """
            abc .cite {einstein} def .cite {latexcompanion} ghi .cite {knuthwebsite}

            $BIBLIOGRAPHY_CALL

            abc .cite {einstein} def .cite {latexcompanion} ghi .cite {knuthwebsite}
            """.trimIndent(),
            renderer = { rendererFactory, ctx -> rendererFactory.plainText(ctx) },
        ) {
            assertEquals(
                "abc [1] def [2] ghi [3]\n\n" +
                    "[1] A. Einstein, \u201CZur Elektrodynamik bewegter K\u00F6rper. (German) " +
                    "[On the electrodynamics of moving bodies],\u201D Annalen der Physik, " +
                    "vol. 322, Art. no. 10, 1905, doi: http://dx.doi.org/10.1002/andp.19053221004.\n" +
                    "[2] M. Goossens, F. Mittelbach, and A. Samarin, The LaTeX Companion. " +
                    "Reading, Massachusetts: Addison-Wesley, 1993.\n" +
                    "[3] D. Knuth, \u201CKnuth: Computers and Typesetting.\u201D [Online]. Available: " +
                    "http://www-cs-faculty.stanford.edu/uno/abcde.html\n\n" +
                    "abc [1] def [2] ghi [3]\n\n",
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
                    IEEE_BIBLIOGRAPHY_OUTPUT,
                it,
            )
        }
    }
}
