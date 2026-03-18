package com.quarkdown.test

import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for Markdown nodes.
 * @see LinkTest for link and image tests
 */
class NodesTest {
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
