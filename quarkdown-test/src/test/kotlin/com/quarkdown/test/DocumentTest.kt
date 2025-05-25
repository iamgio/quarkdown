package com.quarkdown.test

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.quarkdown.block.Container.Alignment
import com.quarkdown.core.document.DocumentAuthor
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.page.PageOrientation
import com.quarkdown.core.document.page.PageSizeFormat
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
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
            .pageformat {A3} orientation:{landscape} margin:{3cm 2px} columns:{4} alignment:{end}
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
                assertEquals(bounds.width, documentInfo.layout.pageFormat.pageWidth)
                assertEquals(bounds.height, documentInfo.layout.pageFormat.pageHeight)
            }

            assertEquals(
                Sizes(
                    vertical = Size(3.0, Size.Unit.CENTIMETERS),
                    horizontal = Size(2.0, Size.Unit.PIXELS),
                ),
                documentInfo.layout.pageFormat.margin,
            )

            assertEquals(4, documentInfo.layout.pageFormat.columnCount)
            assertEquals(Alignment.END, documentInfo.layout.pageFormat.alignment)
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
}
