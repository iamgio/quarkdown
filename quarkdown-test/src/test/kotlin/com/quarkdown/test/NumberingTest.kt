package com.quarkdown.test

import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for numbering of headings, figures, tables and other elements.
 */
class NumberingTest {
    @Test
    fun `no numbering`() {
        // Numbering is disabled by default.
        execute(
            """
            .noautopagebreak
            # A
            ## A/1
            # B
            ![](img.png '')
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>A</h1>" +
                    "<h2>A/1</h2>" +
                    "<h1>B</h1>" +
                    "<figure><img src=\"img.png\" alt=\"\" title=\"\" /><figcaption class=\"caption-bottom\"></figcaption></figure>",
                it,
            )
        }
    }

    @Test
    fun `heading numbering`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - headings: 1.1
            # A
            # B
            # C
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<h1 data-location=\"3\">C</h1>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            .numbering
               - headings: 1.1
            # A
            ## A/1
            # B
            # C
            ## C/1
            ## C/2
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<h2 data-location=\"1.1\">A/1</h2>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<h1 data-location=\"3\">C</h1>" +
                    "<h2 data-location=\"3.1\">C/1</h2>" +
                    "<h2 data-location=\"3.2\">C/2</h2>",
                it,
            )
        }

        // Decorative headings are not numbered.
        execute(
            """
            .noautopagebreak
            .numbering
               - headings: 1.1
            # A
            ## A/1
            #! Nope!
            # B
            # C
            ## C/1
            ## C/2
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<h2 data-location=\"1.1\">A/1</h2>" +
                    "<h1>Nope!</h1>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<h1 data-location=\"3\">C</h1>" +
                    "<h2 data-location=\"3.1\">C/1</h2>" +
                    "<h2 data-location=\"3.2\">C/2</h2>",
                it,
            )
        }

        // Roman numerals.
        execute(
            """
            .noautopagebreak
            .numbering
                - headings: I::i
            # A
            ## A/1
            # B
            # C
            ## C/1
            ## C/2
            # D
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"I\">A</h1>" +
                    "<h2 data-location=\"I::i\">A/1</h2>" +
                    "<h1 data-location=\"II\">B</h1>" +
                    "<h1 data-location=\"III\">C</h1>" +
                    "<h2 data-location=\"III::i\">C/1</h2>" +
                    "<h2 data-location=\"III::ii\">C/2</h2>" +
                    "<h1 data-location=\"IV\">D</h1>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            .numbering
                - headings: A.a.1
            # A
            ## A/1
            ### A/1/1
            ## A/2
            # B
            ### B/0/1
            # C
            ## C/1
            ### C/1/1
            ## C/2
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"A\">A</h1>" +
                    "<h2 data-location=\"A.a\">A/1</h2>" +
                    "<h3 data-location=\"A.a.1\">A/1/1</h3>" +
                    "<h2 data-location=\"A.b\">A/2</h2>" +
                    "<h1 data-location=\"B\">B</h1>" +
                    "<h3 data-location=\"B.0.1\">B/0/1</h3>" +
                    "<h1 data-location=\"C\">C</h1>" +
                    "<h2 data-location=\"C.a\">C/1</h2>" +
                    "<h3 data-location=\"C.a.1\">C/1/1</h3>" +
                    "<h2 data-location=\"C.b\">C/2</h2>",
                it,
            )
        }

        // Nesting levels that don't fit in the numbering format are ignored.
        execute(
            """
            .noautopagebreak
            .numbering
                - headings: 1.1
            # A
            ## A/1
            ### A/1/1
            # B
            ### B/1/1
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<h2 data-location=\"1.1\">A/1</h2>" +
                    "<h3>A/1/1</h3>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<h3>B/1/1</h3>",
                it,
            )
        }
    }

    @Test
    fun `default numbering`() {
        // Default numbering set by the document type.
        execute(
            """
            .doctype {paged}
            .noautopagebreak
            # A
            ## A/1
            # B
            # C
            ## C/1
            ### C/1/1
            ## C/2
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<h2 data-location=\"1.1\">A/1</h2>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<h1 data-location=\"3\">C</h1>" +
                    "<h2 data-location=\"3.1\">C/1</h2>" +
                    "<h3 data-location=\"3.1.1\">C/1/1</h3>" +
                    "<h2 data-location=\"3.2\">C/2</h2>",
                it,
            )
        }

        // Disable default numbering.
        execute(
            """
            .doctype {paged}
            .nonumbering
            .noautopagebreak
            # A
            ## A/1
            # B
            
            ![](img.png "Caption")
            
            | A | B | C |
            |---|---|---|
            | D | E | F |
            ''
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>A</h1>" +
                    "<h2>A/1</h2>" +
                    "<h1>B</h1>" +
                    "<figure><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\">Caption</figcaption></figure>" +
                    "<table><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>" +
                    "<tbody><tr><td>D</td><td>E</td><td>F</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\"></caption></table>",
                it,
            )
        }
    }

    @Test
    fun `figure and table numbering`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - headings: 1.1.1
                - figures: 1.1
            
            # A
            
            ![](img.png "Caption 1")
            
            ![](img.png "Caption 2")
            
            # B
            
            ![](img.png "")
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<figure id=\"figure-1.1\"><img src=\"img.png\" alt=\"\" title=\"Caption 1\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.1\">Caption 1</figcaption>" +
                    "</figure>" +
                    "<figure id=\"figure-1.2\"><img src=\"img.png\" alt=\"\" title=\"Caption 2\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.2\">Caption 2</figcaption>" +
                    "</figure>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<figure id=\"figure-2.1\"><img src=\"img.png\" alt=\"\" title=\"\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"2.1\"></figcaption>" +
                    "</figure>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            .numbering
                - headings: 1
                - figures: 1.A.a
                - tables: 1.A.a
            
            ![](img.png "Caption")
            
            # A
            
            ![](img.png "Caption")
            
            | A | B | C |
            |---|---|---|
            | D | E | F |
            'Table caption'
            
            ## A/1
            
            ![](img.png "Caption")
            
            ### A/1/1
            
            ![](img.png "Caption")
            
            | A | B | C |
            |---|---|---|
            | D | E | F |
            ''
            
            # B
            
            ![](img.png "Caption")
            
            ### B/0/1
            
            ![](img.png "Caption")
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<figure id=\"figure-0.0.a\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"0.0.a\">Caption</figcaption>" +
                    "</figure>" +
                    "<h1 data-location=\"1\">A</h1>" +
                    "<figure id=\"figure-1.0.a\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.0.a\">Caption</figcaption>" +
                    "</figure>" +
                    "<table id=\"table-1.0.a\"><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>" +
                    "<tbody><tr><td>D</td><td>E</td><td>F</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\" data-element-label=\"1.0.a\">Table caption</caption></table>" +
                    "<h2>A/1</h2>" +
                    "<figure id=\"figure-1.A.a\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.A.a\">Caption</figcaption>" +
                    "</figure>" +
                    "<h3>A/1/1</h3>" +
                    "<figure id=\"figure-1.A.b\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.A.b\">Caption</figcaption>" +
                    "</figure>" +
                    "<table id=\"table-1.A.a\"><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>" +
                    "<tbody><tr><td>D</td><td>E</td><td>F</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\" data-element-label=\"1.A.a\"></caption></table>" +
                    "<h1 data-location=\"2\">B</h1>" +
                    "<figure id=\"figure-2.0.a\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"2.0.a\">Caption</figcaption>" +
                    "</figure>" +
                    "<h3>B/0/1</h3>" +
                    "<figure id=\"figure-2.0.b\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"2.0.b\">Caption</figcaption>" +
                    "</figure>",
                it,
            )
        }

        // Non-captioned elements are not counted.
        execute(
            """
            .noautopagebreak
            .numbering
                - figures: 1.1
                - tables: 1.1
            
            # A
            
            ![](img.png)
            
            | A | B | C |
            |---|---|---|
            | D | E | F |
            
            ![](img.png "Caption")
            
            | A | B | C |
            |---|---|---|
            | D | E | F |
            'Caption'
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>A</h1>" +
                    "<figure><img src=\"img.png\" alt=\"\" /></figure>" +
                    "<table><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>" +
                    "<tbody><tr><td>D</td><td>E</td><td>F</td></tr></tbody></table>" +
                    "<figure id=\"figure-1.1\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.1\">Caption</figcaption>" +
                    "</figure>" +
                    "<table id=\"table-1.1\"><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>" +
                    "<tbody><tr><td>D</td><td>E</td><td>F</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\" data-element-label=\"1.1\">Caption</caption></table>",
                it,
            )
        }
    }

    @Test
    fun `mermaid diagram numbered as figure`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - headings: 1.1.1
                - figures: 1.1
            
            # A
            
            ![](img.png "Caption 1")
            
            .mermaid caption:{Caption 2}
                graph TD
                    A-->B
                    A-->C
            
            ![](img.png "Caption 3")
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1 data-location=\"1\">A</h1>" +
                    "<figure id=\"figure-1.1\"><img src=\"img.png\" alt=\"\" title=\"Caption 1\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.1\">Caption 1</figcaption>" +
                    "</figure>" +
                    "<figure id=\"figure-1.2\">" +
                    "<pre class=\"mermaid fill-height\">graph TD\n    A-->B\n    A-->C</pre>" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.2\">Caption 2</figcaption>" +
                    "</figure>" +
                    "<figure id=\"figure-1.3\"><img src=\"img.png\" alt=\"\" title=\"Caption 3\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.3\">Caption 3</figcaption>" +
                    "</figure>",
                it,
            )
        }
    }

    @Test
    fun `custom figure`() {
        execute(
            """
            .doclang {en}
            .numbering
                - figures: 1
            
            .figure {My caption.}
                Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                "<figure id=\"figure-1\"><p>Hello, world!</p>" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1\" data-localized-kind=\"Figure\">" +
                    "My caption.</figcaption></figure>",
                it,
            )
        }
    }

    @Test
    fun `localized numbering captions`() {
        execute(
            """
            .noautopagebreak
            .doclang {italian}
            .numbering
                - headings: none
                - figures: 1.1
                - tables: 1.a
            
            # A
            
            ![](img.png "Caption")
            
            | A | B | C |
            |---|---|---|
            | D | E | F |
            (Caption)
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>A</h1>" +
                    "<figure id=\"figure-1.1\"><img src=\"img.png\" alt=\"\" title=\"Caption\" />" +
                    "<figcaption class=\"caption-bottom\" data-element-label=\"1.1\" data-localized-kind=\"Figura\">Caption</figcaption>" +
                    "</figure>" +
                    "<table id=\"table-1.a\"><thead><tr><th>A</th><th>B</th><th>C</th></tr></thead>" +
                    "<tbody><tr><td>D</td><td>E</td><td>F</td></tr></tbody>" +
                    "<caption class=\"caption-bottom\" data-element-label=\"1.a\" data-localized-kind=\"Tabella\">Caption</caption></table>",
                it,
            )
        }
    }

    @Test
    fun `custom numbering`() {
        execute(
            """
            .noautopagebreak
            .numbering
                - key1: 1.1
                - key2: A
            
            # 1
            
            .numbered {key1}
                num:
                Hello, .num!
                
            .numbered {key1}
                num:
                Hello again, .num!
                
            .numbered {key2}
                num:
                Hi, .num!
            
            # 2
            
            .numbered {key1}
                num:
                Hi, .num!
                
            .numbered {key2}
                Hey, .1!
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>1</h1>" +
                    "<p>Hello, 1.1!</p>" +
                    "<p>Hello again, 1.2!</p>" +
                    "<p>Hi, A!</p>" +
                    "<h1>2</h1>" +
                    "<p>Hi, 2.1!</p>" +
                    "<p>Hey, B!</p>",
                it,
            )
        }
    }

    /**
     * To understand why this is a special case, see [com.quarkdown.core.ast.quarkdown.block.Numbered]'s documentation.
     */
    @Test
    fun `custom numbering on node`() {
        execute(
            """
            .numbering
                - key: 1
            
            .numbered {key}
                num:
                .container
                    Hello, .num
            """.trimIndent(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertEquals(
                "<div class=\"container\">" +
                    "<p>Hello, 1</p>" +
                    "</div>",
                it,
            )
        }
    }

    /**
     * To understand why this is a special case, see [com.quarkdown.core.ast.quarkdown.block.Numbered]'s documentation.
     */
    @Test
    fun `error handling in custom numbering`() {
        execute(
            """
            .numbering
                - key: 1
            
            .numbered {key}
                .sum {1} {a}
            """.trimIndent(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertTrue(it.startsWith("<div class=\"box error\"><header><h4>Error: sum</h4>"))
        }
    }
}
