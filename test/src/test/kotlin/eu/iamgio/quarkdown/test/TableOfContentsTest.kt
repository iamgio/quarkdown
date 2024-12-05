package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.test.util.DEFAULT_OPTIONS
import eu.iamgio.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for tables of contents.
 */
class TableOfContentsTest {
    @Test
    fun `table of contents`() {
        execute(
            """
            .tableofcontents
            
            # ABC
            
            Hi
            
            # DEF
            
            Hello
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"table-of-contents\"></h1>" +
                    "<nav><ol>" +
                    "<li><a href=\"#abc\">ABC</a></li>" +
                    "<li><a href=\"#def\">DEF</a></li>" +
                    "</ol></nav>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"abc\">ABC</h1><p>Hi</p>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"def\">DEF</h1>" +
                    "<p>Hello</p>",
                it,
            )
        }

        execute(
            """
            .tableofcontents title:{_TOC_}
            
            # ABC
            
            Hi
            
            ## _ABC/1_
            
            Hello
            
            # DEF
            
            DEF/1
            ---
            
            Hi there
            
            ### DEF/2
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"table-of-contents\"><em>TOC</em></h1>" +
                    "<nav><ol>" +
                    "<li><a href=\"#abc\">ABC</a>" +
                    "<ol><li><a href=\"#abc1\">ABC/1</a></li></ol></li>" +
                    "<li><a href=\"#def\">DEF</a>" +
                    "<ol><li><a href=\"#def1\">DEF/1</a>" +
                    "<ol><li><a href=\"#def2\">DEF/2</a></li>" +
                    "</ol></li></ol></li>" +
                    "</ol></nav>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"abc\">ABC</h1><p>Hi</p>" +
                    "<h2 id=\"abc1\"><em>ABC/1</em></h2><p>Hello</p>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"def\">DEF</h1>" +
                    "<h2 id=\"def1\">DEF/1</h2>" +
                    "<p>Hi there</p>" +
                    "<h3 id=\"def2\">DEF/2</h3>",
                it,
            )
        }
    }

    @Test
    fun `localized table of contents title`() {
        execute(
            """
            .doclang {english}
            .tableofcontents
            
            # ABC
            
            Hi
            
            # DEF
            
            Hello
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"table-of-contents\">Table of Contents</h1>" + // Localized name
                    "<nav><ol>" +
                    "<li><a href=\"#abc\">ABC</a></li>" +
                    "<li><a href=\"#def\">DEF</a></li>" +
                    "</ol></nav>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"abc\">ABC</h1><p>Hi</p>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"def\">DEF</h1>" +
                    "<p>Hello</p>",
                it,
            )
        }
    }

    @Test
    fun `table of contents markers`() {
        execute(
            """
            .tableofcontents title:{***TOC***} maxdepth:{0}
            
            .marker {*Marker 1*}
            
            # ABC
            
            .marker {*Marker 2*}
            
            ## DEF
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"table-of-contents\"><em><strong>TOC</strong></em></h1>" +
                    "<nav><ol>" +
                    "<li><a href=\"#marker-1\">Marker 1</a></li>" +
                    "<li><a href=\"#marker-2\">Marker 2</a></li>" +
                    "</ol></nav>" +
                    "<div class=\"marker\" data-hidden=\"\" id=\"marker-1\"></div>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"abc\">ABC</h1>" +
                    "<div class=\"marker\" data-hidden=\"\" id=\"marker-2\"></div>" +
                    "<h2 id=\"def\">DEF</h2>",
                it,
            )
        }
    }

    @Test
    fun `table of contents focus`() {
        // Focus
        execute(
            """
            .tableofcontents title:{TOC} focus:{DEF}
            
            # ABC
            
            ## X
            
            # DEF
            
            ## Y
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true),
        ) {
            assertEquals(
                "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"table-of-contents\">TOC</h1>" +
                    "<nav><ol>" +
                    "<li><a href=\"#abc\">ABC</a><ol><li><a href=\"#x\">X</a></li></ol></li>" +
                    "<li class=\"focused\"><a href=\"#def\">DEF</a><ol><li><a href=\"#y\">Y</a></li></ol></li>" +
                    "</ol></nav>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"abc\">ABC</h1>" +
                    "<h2 id=\"x\">X</h2>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"def\">DEF</h1>" +
                    "<h2 id=\"y\">Y</h2>",
                it,
            )
        }
    }

    @Test
    fun `table of contents numbering`() {
        // Numbering
        execute(
            """
            .numbering
               - headings: 1.A.a
            .noautopagebreak
            .tableofcontents title:{TOC}
            
            # A            
            ## A/1
            ### A/1/1
            ## A/2
            # B
            ### B/0/1
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableAutomaticIdentifiers = true, enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 id=\"table-of-contents\">TOC</h1>" +
                    "<nav><ol>" +
                    "<li data-location=\"1\"><a href=\"#a\">A</a>" +
                    "<ol><li data-location=\"1.A\"><a href=\"#a1\">A/1</a>" +
                    "<ol><li data-location=\"1.A.a\"><a href=\"#a11\">A/1/1</a></li></ol></li>" +
                    "<li data-location=\"1.B\"><a href=\"#a2\">A/2</a></li></ol></li>" +
                    "<li data-location=\"2\"><a href=\"#b\">B</a>" +
                    "<ol><li data-location=\"2.0.a\"><a href=\"#b01\">B/0/1</a></li></ol></li>" +
                    "</ol></nav>" +
                    "<h1 id=\"a\" data-location=\"1\">A</h1>" +
                    "<h2 id=\"a1\" data-location=\"1.A\">A/1</h2>" +
                    "<h3 id=\"a11\" data-location=\"1.A.a\">A/1/1</h3>" +
                    "<h2 id=\"a2\" data-location=\"1.B\">A/2</h2>" +
                    "<h1 id=\"b\" data-location=\"2\">B</h1>" +
                    "<h3 id=\"b01\" data-location=\"2.0.a\">B/0/1</h3>",
                it,
            )
        }
    }
}
