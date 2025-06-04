package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Markdown nodes.
 */
class NodesTest {
    @Test
    fun text() {
        execute("Hello, world!") {
            assertEquals("<p>Hello, world!</p>", it)
        }

        execute(
            """
            > This is a **"quote"** with 'text *replacement*'.  
            > This is a feature of Quarkdown - the Turing complete Markdown - by iamgio (C) 2024.
            > => Quarkdown != other Markdown flavors... <-
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote><p>" +
                    "This is a <strong>&ldquo;quote&rdquo;</strong> with &lsquo;text <em>replacement</em>&rsquo;.<br />" +
                    "This is a feature of Quarkdown &mdash; the Turing complete Markdown &mdash; by iamgio &copy; 2024.\n" +
                    "&rArr; Quarkdown &ne; other Markdown flavors&hellip; &larr;" +
                    "</p></blockquote>",
                it,
            )
        }

        execute(".noautopagebreak\n# Title\n Hello, world!\n## Subtitle\nHello, world!") {
            assertEquals(
                "<h1>Title</h1><p>Hello, world!</p><h2>Subtitle</h2><p>Hello, world!</p>",
                it,
            )
        }

        execute("Hello, **world**! [_link_](https://example.com \"title\")") {
            assertEquals(
                "<p>Hello, <strong>world</strong>! <a href=\"https://example.com\" title=\"title\"><em>link</em></a></p>",
                it,
            )
        }

        execute("This is a .text {small text} size:{tiny} variant:{smallcaps}") {
            assertEquals(
                "<p>This is a <span class=\"size-tiny\" style=\"font-variant: small-caps;\">small text</span></p>",
                it,
            )
        }

        execute(
            """
            Line 1
            
            .whitespace
            
            Line 2 after a long break
            """.trimIndent(),
        ) {
            assertEquals("<p>Line 1</p><span>&nbsp;</span><p>Line 2 after a long break</p>", it)
        }

        execute("A .whitespace width:{1cm} B") {
            assertEquals("<p>A <div style=\"width: 1.0cm;\"></div> B</p>", it)
        }

        execute("A .whitespace width:{1cm} height:{3mm} B") {
            assertEquals("<p>A <div style=\"width: 1.0cm; height: 3.0mm;\"></div> B</p>", it)
        }

        execute("Hello, World! .uppercase {Hello, World!} .lowercase {Hello, World!} .capitalize {hello, world!}") {
            assertEquals(
                "<p>Hello, World! HELLO, WORLD! hello, world! Hello, world!</p>",
                it,
            )
        }

        execute(
            """
            .doclang {Italian}
            > Tip: you could try Quarkdown.  
            > It's a cool language!
            > - **iamgio**
            
            > Important: leave a feedback!
            """.trimIndent(),
        ) {
            assertEquals(
                "<blockquote class=\"tip\" style=\"--quote-type-label: 'Consiglio';\" data-labeled=\"\">" +
                    "<p>you could try Quarkdown.<br />" +
                    "It&rsquo;s a cool language!</p>" +
                    "<p class=\"attribution\"><strong>iamgio</strong></p>" +
                    "</blockquote>" +
                    "<blockquote class=\"important\" style=\"--quote-type-label: 'Importante';\" data-labeled=\"\">" +
                    "<p>leave a feedback!</p>" +
                    "</blockquote>",
                it,
            )
        }

        execute(
            """
            A
            
            .align {end}
                ### B
            C
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>A</p>" +
                    "<div class=\"container fullwidth\" style=\"justify-items: end; text-align: end;\"><h3>B</h3></div>" +
                    "<p>C</p>",
                it,
            )
        }
    }

    @Test
    fun headings() {
        execute("# Title") {
            assertEquals("<div class=\"page-break\" data-hidden=\"\"></div><h1>Title</h1>", it)
        }

        execute("## Ti*tl*e") {
            assertEquals("<h2>Ti<em>tl</em>e</h2>", it)
        }

        execute("#! Title") {
            assertEquals("<h1>Title</h1>", it)
        }

        execute("#### .sum {3} {2}") {
            assertEquals("<h4>5</h4>", it)
        }

        execute("###### .text {Hello, **world**} size:{tiny}") {
            assertEquals("<h6><span class=\"size-tiny\">Hello, <strong>world</strong></span></h6>", it)
        }

        execute(
            """
            .autopagebreak maxdepth:{4}
            ## A
            ### B
            ##### C
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h2>A</h2>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h3>B</h3>" +
                    "<h5>C</h5>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            # A
            """.trimIndent(),
        ) {
            assertEquals("<h1>A</h1>", it)
        }
    }

    @Test
    fun links() {
        execute("This is a link: [link](https://example.com 'title')") {
            assertEquals("<p>This is a link: <a href=\"https://example.com\" title=\"title\">link</a></p>", it)
            assertTrue(attributes.linkDefinitions.isEmpty())
        }

        execute(
            """
            [Link definition]: https://example.com
            **This is a link**: [link][Link definition]
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><strong>This is a link</strong>: <a href=\"https://example.com\">link</a></p>",
                it,
            )
            assertEquals(1, attributes.linkDefinitions.size)
        }

        execute(
            """
            [Link definition]: https://example.com
            ## _This is a link_: [Link definition]
            """.trimIndent(),
        ) {
            assertEquals(
                "<h2><em>This is a link</em>: <a href=\"https://example.com\">Link definition</a></h2>",
                it,
            )
            assertEquals(1, attributes.linkDefinitions.size)
        }

        execute(
            """
            This link does not exist: [link][Link definition]
            """.trimIndent(),
        ) {
            assertEquals("<p>This link does not exist: [link][Link definition]</p>", it)
            assertTrue(attributes.linkDefinitions.isEmpty())
        }

        execute(".text {Hello} size:{tiny} url:{https://example.com}") {
            assertEquals("<a href=\"https://example.com\"><span class=\"size-tiny\">Hello</span></a>", it)
        }

        execute(".text {Hello} size:{tiny} url:{.concatenate {https://example} {\\.com}}") {
            assertEquals("<a href=\"https://example.com\"><span class=\"size-tiny\">Hello</span></a>", it)
        }
    }

    @Test
    fun images() {
        execute("Some image: ![Alt text](https://example.com/image.png)") {
            assertEquals("<p>Some image: <img src=\"https://example.com/image.png\" alt=\"Alt text\" /></p>", it)
        }

        execute("![Alt text](https://example.com/image.png)") {
            assertEquals("<figure><img src=\"https://example.com/image.png\" alt=\"Alt text\" /></figure>", it)
        }

        execute("![Alt text](https://example.com/image.png 'Title')") {
            assertEquals(
                "<figure>" +
                    "<img src=\"https://example.com/image.png\" alt=\"Alt text\" title=\"Title\" />" +
                    "<figcaption class=\"caption-bottom\">Title</figcaption>" +
                    "</figure>",
                it,
            )
        }

        execute("Sized image: !(20x_)[Alt text](https://example.com/image.png)") {
            assertEquals(
                "<p>Sized image: <img src=\"https://example.com/image.png\" alt=\"Alt text\" style=\"width: 20.0px;\" /></p>",
                it,
            )
        }

        execute("!(2in*2.1cm)[Alt text](https://example.com/image.png)") {
            assertEquals(
                "<figure><img src=\"https://example.com/image.png\" alt=\"Alt text\" style=\"width: 2.0in; height: 2.1cm;\" /></figure>",
                it,
            )
        }

        execute(
            """
            [Alt text]: https://example.com/image.png
            ![Alt text][Alt text]
            """.trimIndent(),
        ) {
            assertEquals("<p><img src=\"https://example.com/image.png\" alt=\"Alt text\" /></p>", it)
            assertEquals(1, attributes.linkDefinitions.size)
        }

        execute(
            """
            [Alt text]: https://example.com/image.png
            ## ![Alt text][Alt text]
            """.trimIndent(),
        ) {
            assertEquals("<h2><img src=\"https://example.com/image.png\" alt=\"Alt text\" /></h2>", it)
            assertEquals(1, attributes.linkDefinitions.size)
        }

        execute(
            """
            This image does not exist: ![Alt text][Alt text]
            """.trimIndent(),
        ) {
            assertEquals("<p>This image does not exist: ![Alt text][Alt text]</p>", it)
            assertTrue(attributes.linkDefinitions.isEmpty())
        }
    }

    @Test
    fun lists() {
        execute("- Item 1\n- Item 2\n  - Item 2.1\n  - Item 2.2\n- Item 3") {
            assertEquals(
                "<ul><li>Item 1</li><li>Item 2<ul><li>Item 2.1</li><li>Item 2.2</li></ul></li><li>Item 3</li></ul>",
                it,
            )
        }

        execute("1. Item 1\n2. Item 2\n   1. Item 2.1\n   2. Item 2.2\n3. Item 3") {
            assertEquals(
                "<ol><li>Item 1</li><li>Item 2<ol><li>Item 2.1</li><li>Item 2.2</li></ol></li><li>Item 3</li></ol>",
                it,
            )
        }

        execute("- [ ] Unchecked\n- [x] Checked") {
            assertEquals(
                "<ul><li class=\"task-list-item\"><input disabled=\"\" type=\"checkbox\" />Unchecked</li>" +
                    "<li class=\"task-list-item\"><input disabled=\"\" type=\"checkbox\" checked=\"\" />Checked</li></ul>",
                it,
            )
        }
    }

    @Test
    fun `block html`() {
        execute(
            """
            Hello
            
            .html
                <p><sup>World</sup></p>
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello</p><p><sup>World</sup></p>",
                it,
            )
        }
    }

    @Test
    fun `inline html`() {
        execute("Hello, .html {<sup>World</sup>}") {
            assertEquals(
                "<p>Hello, <sup>World</sup></p>",
                it,
            )
        }
    }
}
