package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.document.DocumentAuthor
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.document.page.PageOrientation
import eu.iamgio.quarkdown.document.page.PageSizeFormat
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for document metadata and attributes.
 */
class DocumentTest {
    @Test
    fun `initial state`() {
        execute("") {
            assertEquals("", it)
            assertIs<AstRoot>(attributes.root)
            assertFalse(attributes.hasCode)
            assertFalse(attributes.hasMath)
            assertTrue(attributes.linkDefinitions.isEmpty())
            assertEquals(DocumentType.PLAIN, documentInfo.type)
            assertNull(documentInfo.name)
            assertEquals(0, documentInfo.authors.size)
            assertNull(documentInfo.locale)
        }
    }

    @Test
    fun `document setup`() {
        execute(
            """
            .docname {My Quarkdown document}
            .docauthors
              - iamgio
                - website: https://iamgio.eu
              - Giorgio
                - website: https://github.com/iamgio
              - Gio
            .doctype {slides}
            .doclang {english}
            .theme {darko} layout:{minimal}
            .pageformat {A3} orientation:{landscape} margin:{3cm 2px} columns:{4}
            .slides transition:{zoom} speed:{fast}
            .autopagebreak maxdepth:{3}
            """.trimIndent(),
        ) {
            assertEquals("My Quarkdown document", documentInfo.name)
            assertEquals(
                listOf(
                    DocumentAuthor("iamgio", mapOf("website" to "https://iamgio.eu")),
                    DocumentAuthor("Giorgio", mapOf("website" to "https://github.com/iamgio")),
                    DocumentAuthor("Gio", mapOf()),
                ),
                documentInfo.authors,
            )
            assertEquals("en", documentInfo.locale?.tag)
            assertEquals(DocumentType.SLIDES, documentInfo.type)
            assertEquals("darko", documentInfo.theme?.color)
            assertEquals("minimal", documentInfo.theme?.layout)

            PageSizeFormat.A3.getBounds(PageOrientation.LANDSCAPE).let { bounds ->
                assertEquals(bounds.width, documentInfo.pageFormat.pageWidth)
                assertEquals(bounds.height, documentInfo.pageFormat.pageHeight)
            }

            assertEquals(
                Sizes(
                    vertical = Size(3.0, Size.Unit.CM),
                    horizontal = Size(2.0, Size.Unit.PX),
                ),
                documentInfo.pageFormat.margin,
            )

            assertEquals(4, documentInfo.pageFormat.columnCount)
        }
    }

    @Test
    fun `document metadata echo`() {
        execute(
            """
            .docname {My Quarkdown document}
            .docauthors
              - iamgio
                - country: Italy
            .doctype {slides}
            .doclang {english}
            
            .docname .text {.docname} size:{tiny}.
            
            .docauthors
            
            #! .docauthor
            
            .doctype
            
            .doclang
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>My Quarkdown document " +
                    "<span class=\"size-tiny\">My Quarkdown document</span>.</p>" +
                    "<table>" +
                    "<thead><tr><th>Key</th><th>Value</th></tr></thead>" +
                    "<tbody>" +
                    "<tr><td>iamgio</td><td>" +
                    "<table><thead><tr><th>Key</th><th>Value</th></tr></thead>" +
                    "<tbody><tr><td>country</td><td><p>Italy</p></td></tr></tbody></table></td></tr>" +
                    "</tbody>" +
                    "</table>" +
                    "<h1>iamgio</h1>" +
                    "<p>slides</p>" +
                    "<p>English</p>",
                it,
            )
        }
    }

    @Test
    fun localization() {
        execute(
            """
            .doclang {english}
            .localization {mytable}
                - English
                    - morning: Good morning
                    - evening: Good evening
                - Italian
                    - morning: Buongiorno
                    - evening: Buonasera
            
            > .localize {mytable:morning}.
            """.trimIndent(),
        ) {
            assertEquals("<blockquote><p>Good morning.</p></blockquote>", it)
        }

        execute(
            """
            .doclang {italian}
            .localization {mytable}
                - English
                    - theorem: Theorem
                - Italian
                    - theorem: Teorema

            .function {theorem}
                **.localize {mytable:theorem}.**

            .theorem Test
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Teorema.</strong> Test</p>", it)
        }

        assertFails {
            execute(
                """
                .doclang {english}
                .localization {mytable}
                    - English
                        - morning: Good morning
                        - evening: Good evening
                    - Italian
                        - morning: Buongiorno
                        - evening: Buonasera
                
                > .localize {mytable:afternoon}.
                """.trimIndent(),
            ) {}
        }

        // Library's injected localization table.
        execute(
            """
            .doclang {english}
            .localize {std:warning}
            """.trimIndent(),
        ) {
            assertEquals("<p>Warning</p>", it)
        }
    }
}