package com.quarkdown.test

import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.test.util.execute
import com.quarkdown.test.util.getSubdocumentResourceCount
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for links, reference links, and reference images.
 */
class LinkTest {
    @Test
    fun link() {
        execute("This is a link: [link](https://example.com 'title')") {
            assertEquals("<p>This is a link: <a href=\"https://example.com\" title=\"title\">link</a></p>", it)
        }
    }

    @Test
    fun `text link`() {
        execute(".text {Hello} size:{tiny} url:{https://example.com}") {
            assertEquals("<a href=\"https://example.com\"><span class=\"size-tiny\">Hello</span></a>", it)
        }

        execute(".text {Hello} size:{tiny} url:{.concatenate {https://example} {\\.com}}") {
            assertEquals("<a href=\"https://example.com\"><span class=\"size-tiny\">Hello</span></a>", it)
        }
    }

    @Test
    fun `reference link`() {
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
        }
    }

    @Test
    fun `collapsed reference link`() {
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
        }
    }

    @Test
    fun `unresolved reference link`() {
        execute(
            """
            This link does not exist: [link][Link definition]
            """.trimIndent(),
        ) {
            assertEquals("<p>This link does not exist: [link][Link definition]</p>", it)
        }
    }

    @Test
    fun `reference link with title`() {
        execute(
            """
            [ref]: https://example.com "Title"
            [ref]
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><a href=\"https://example.com\" title=\"Title\">ref</a></p>",
                it,
            )
        }
    }

    @Test
    fun `multiple reference links to same definition`() {
        execute(
            """
            [ref]: https://example.com
            [A][ref] and [B][ref]
            """.trimIndent(),
        ) {
            assertEquals(
                "<p><a href=\"https://example.com\">A</a> and <a href=\"https://example.com\">B</a></p>",
                it,
            )
        }
    }

    @Test
    fun `reference image`() {
        execute(
            """
            [Alt text]: https://example.com/image.png
            ![Alt text][Alt text]
            """.trimIndent(),
        ) {
            assertEquals("<p><img src=\"https://example.com/image.png\" alt=\"Alt text\" /></p>", it)
        }
    }

    @Test
    fun `reference image in heading`() {
        execute(
            """
            [Alt text]: https://example.com/image.png
            ## ![Alt text][Alt text]
            """.trimIndent(),
        ) {
            assertEquals("<h2><img src=\"https://example.com/image.png\" alt=\"Alt text\" /></h2>", it)
        }
    }

    @Test
    fun `unresolved reference image`() {
        execute(
            """
            This image does not exist: ![Alt text][Alt text]
            """.trimIndent(),
        ) {
            assertEquals("<p>This image does not exist: ![Alt text][Alt text]</p>", it)
        }
    }

    // Root path symbol (@)

    @Test
    fun `bare root path symbol in link from root`() {
        execute("[Root](@)") {
            assertEquals(
                "<p><a href=\".\">Root</a></p>",
                it,
            )
        }
    }

    @Test
    fun `root path symbol in link from root`() {
        execute("[Root link](@/img/icon.png)") {
            assertEquals(
                "<p><a href=\"./img/icon.png\">Root link</a></p>",
                it,
            )
        }
    }

    @Test
    fun `root path symbol in image from root`() {
        execute("![](@/img/icon.png)") {
            assertEquals(
                "<figure><img src=\"./img/icon.png\" alt=\"\" /></figure>",
                it,
            )
        }
    }

    @Test
    fun `root path symbol in image primitive from root`() {
        execute(".image {@/img/icon.png} label:{Icon} figure:{no} mediastorage:{no}") {
            assertEquals(
                "<img src=\"./img/icon.png\" alt=\"Icon\" />",
                it,
            )
        }
    }

    @Test
    fun `root path symbol in link and image from subdocument`() {
        execute(
            "[1](subdoc/root-path.qd)",
            outputResourceHook = {
                assertEquals(2, getSubdocumentResourceCount(it))
            },
        ) {
            if (subdocument != Subdocument.Root) {
                assertEquals(
                    "<p><a href=\"../img/icon.png\">Root link</a></p>" +
                        "<figure><img src=\"../img/icon.png\" alt=\"\" /></figure>",
                    it,
                )
            }
        }
    }
}
