package com.quarkdown.test

import com.quarkdown.rendering.plaintext.extension.plainText
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val BIBLIOGRAPHY_CALL = ".bibliography {bib/bibliography.bib} breakpage:{no}"

/**
 * Builds the expected IEEE bibliography HTML output.
 * @param availableLabel the localized label for online availability (varies by locale)
 */
private fun ieeeBibliographyOutput(availableLabel: String = "Available:") =
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
        "D. Knuth, \u201CKnuth: Computers and Typesetting.\u201D [Online]. $availableLabel " +
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
                ieeeBibliographyOutput(),
                it,
            )
        }
    }

    @Test
    fun `localized bibliography`() {
        execute(".doclang {en}\n$BIBLIOGRAPHY_CALL") {
            assertEquals(
                "<h1 data-decorative=\"\">" +
                    "References" +
                    "</h1>" +
                    ieeeBibliographyOutput(availableLabel = "Available at:"),
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
                    ieeeBibliographyOutput(),
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
                    ieeeBibliographyOutput() +
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
    fun `bibliography custom heading depth`() {
        execute(".doclang {en}\n$BIBLIOGRAPHY_CALL headingdepth:{3}") {
            assertEquals(
                "<h3 data-decorative=\"\">" +
                    "References" +
                    "</h3>" +
                    ieeeBibliographyOutput(availableLabel = "Available at:"),
                it,
            )
        }
    }

    @Test
    fun `bibliography heading indexed in toc, unnumbered`() {
        execute(
            """
            .doclang {en}
            .noautopagebreak
            .tableofcontents title:{}

            .bibliography {bib/bibliography.bib} indexheading:{yes}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertTrue(
                it.contains(
                    "<li data-target-id=\"references\" data-depth=\"1\">" +
                        "<a href=\"#references\">References</a></li>",
                ),
            )
        }
    }

    @Test
    fun `bibliography heading indexed in toc, numbered`() {
        execute(
            """
            .doclang {en}
            .numbering
               - headings: 1.A.a
            .noautopagebreak
            .tableofcontents title:{}

            .bibliography {bib/bibliography.bib} indexheading:{yes} numberheading:{yes}
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true, enableLocationAwareness = true),
        ) {
            assertTrue(
                it.contains(
                    "<li data-target-id=\"references\" data-depth=\"1\" data-location=\"1\">" +
                        "<a href=\"#references\">References</a></li>",
                ),
            )
        }
    }

    @Test
    fun `multi-key citation`() {
        execute(
            """
            abc .cite {einstein, latexcompanion} def

            $BIBLIOGRAPHY_CALL
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>abc [1], [2] def</p>" +
                    ieeeBibliographyOutput(),
                it,
            )
        }
    }

    @Test
    fun `multi-key citation (apa)`() {
        execute(
            """
            abc .cite {einstein, latexcompanion} def

            .bibliography {bib/bibliography.bib} style:{apa} breakpage:{no}
            """.trimIndent(),
        ) {
            // APA uses "et al." for works with 3+ authors.
            val citation = it.toString().substringAfter("abc ").substringBefore(" def")
            assertEquals(
                "(Einstein, 1905; Goossens et al., 1993)",
                citation,
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
                    ieeeBibliographyOutput(),
                it,
            )
        }
    }

    @Test
    fun `partially unresolved multi-key citation`() {
        execute(
            "abc .cite {einstein, invalidkey}\n\n" +
                BIBLIOGRAPHY_CALL,
        ) {
            assertEquals(
                "<p>abc [???]</p>" +
                    ieeeBibliographyOutput(),
                it,
            )
        }
    }
}
