package com.quarkdown.test

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.document.DocumentAuthor
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.layout.page.PageOrientation
import com.quarkdown.core.document.layout.page.PageSizeFormat
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.misc.color.NamedColor
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.stdlib.pageFormat
import com.quarkdown.stdlib.paragraphStyle
import com.quarkdown.test.util.execute
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
            assertNull(documentInfo.description)
            assertTrue(documentInfo.keywords.isEmpty())
            assertNull(documentInfo.locale)
            assertTrue(documentInfo.layout.pageFormats.isEmpty())
            assertNull(documentInfo.layout.paragraphStyle.spacing)
        }
    }

    @Test
    fun `document setup`() {
        execute(
            """
            .docname {My Quarkdown document}
            .docdescription {A comprehensive guide to Quarkdown}
            
            .dockeywords
              - documentation
              - markdown
              - typesetting
            
            .docauthors
              - iamgio
                - website: https://iamgio.eu
              - Giorgio
                - website: https://github.com/iamgio
              - Gio
            .doctype {slides}
            .doclang {english}
            .theme {darko} layout:{minimal}
            .pageformat size:{A3} orientation:{landscape} margin:{3cm 2px} bordercolor:{green} columns:{4} alignment:{end}
            .paragraphstyle lineheight:{2.0} spacing:{1.5} indent:{2}
            .slides transition:{zoom} speed:{fast}
            .autopagebreak maxdepth:{3}
            """.trimIndent(),
        ) {
            assertEquals("My Quarkdown document", documentInfo.name)
            assertEquals("A comprehensive guide to Quarkdown", documentInfo.description)
            assertEquals(listOf("documentation", "markdown", "typesetting"), documentInfo.keywords)
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

            val pageFormat = documentInfo.layout.pageFormats.last()

            PageSizeFormat.A3.getBounds(PageOrientation.LANDSCAPE).let { bounds ->
                assertEquals(bounds.width, pageFormat.pageWidth)
                assertEquals(bounds.height, pageFormat.pageHeight)
            }

            assertEquals(
                Sizes(
                    vertical = Size(3.0, Size.Unit.CENTIMETERS),
                    horizontal = Size(2.0, Size.Unit.PIXELS),
                ),
                pageFormat.margin,
            )

            assertNull(pageFormat.contentBorderWidth)
            assertEquals(NamedColor.GREEN.color, pageFormat.contentBorderColor)
            assertEquals(4, pageFormat.columnCount)
            assertEquals(Container.TextAlignment.END, pageFormat.alignment)

            assertEquals(2.0, documentInfo.layout.paragraphStyle.lineHeight)
            assertEquals(1.5, documentInfo.layout.paragraphStyle.spacing)
            assertEquals(2.0, documentInfo.layout.paragraphStyle.indent)
        }
    }

    @Test
    fun `document cannot have blank name`() {
        assertFails {
            execute(".docname { }") {}
        }

        execute(".docname { }", errorHandler = BasePipelineErrorHandler()) {
            assertNull(documentInfo.name)
        }
    }

    @Test
    fun `document metadata echo`() {
        execute(
            """
            .docname {My Quarkdown document}
            
            .dockeywords
              - quarkdown
              - markdown
              - documentation
            
            .docauthors
              - iamgio
                - country: Italy
            .doctype {slides}
            .doclang {english}

            .docdescription
                A comprehensive guide to Quarkdown

            .docname .text {.docname} size:{tiny}.

            .docdescription

            .docauthors

            #! .docauthor

            .doctype

            .doclang
            
            .dockeywords
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>My Quarkdown document " +
                    "<span class=\"size-tiny\">My Quarkdown document</span>.</p>" +
                    "<p>A comprehensive guide to Quarkdown</p>" +
                    "<table>" +
                    "<thead><tr><th>Key</th><th>Value</th></tr></thead>" +
                    "<tbody>" +
                    "<tr><td>iamgio</td><td>" +
                    "<table><thead><tr><th>Key</th><th>Value</th></tr></thead>" +
                    "<tbody><tr><td>country</td><td><p>Italy</p></td></tr></tbody></table></td></tr>" +
                    "</tbody>" +
                    "</table>" +
                    "<h1 data-decorative=\"\">iamgio</h1>" +
                    "<p>slides</p>" +
                    "<p>English</p>" +
                    "<ol><li><p>quarkdown</p></li><li><p>markdown</p></li><li><p>documentation</p></li></ol>",
                it,
            )
        }
    }

    @Test
    fun `document info modification from scope`() {
        execute(
            """
            .docname {Original Name}
            
            .if {yes}
                .docname {Modified Name}
            """.trimIndent(),
        ) {
            assertEquals("Modified Name", documentInfo.name)
        }
    }
}
