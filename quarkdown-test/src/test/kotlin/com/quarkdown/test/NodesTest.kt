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
    fun headings() {
        execute("# Title") {
            assertEquals("<div class=\"page-break\" data-hidden=\"\"></div><h1>Title</h1>", it)
        }

        execute("## Ti*tl*e") {
            assertEquals("<h2>Ti<em>tl</em>e</h2>", it)
        }

        execute("#! Title") {
            assertEquals("<h1 data-decorative=\"\">Title</h1>", it)
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
}
