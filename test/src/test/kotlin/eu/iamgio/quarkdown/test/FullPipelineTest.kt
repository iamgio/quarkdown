package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.stdlib.Stdlib
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that cover the whole pipeline from lexing to rendering, including function call expansion.
 * [Stdlib] is used as a library.
 */
class FullPipelineTest {
    /**
     * Executes a Quarkdown source.
     * @param hook action run after rendering
     */
    private fun execute(
        source: String,
        hook: (CharSequence) -> Unit,
    ) {
        val hooks =
            PipelineHooks(
                afterRendering = { hook(it) },
            )

        val pipeline =
            Pipeline(
                MutableContext(QuarkdownFlavor),
                libraries = setOf(Stdlib.library),
                renderer = { rendererFactory, context -> rendererFactory.html(context) },
                hooks,
            )

        pipeline.execute(source)
    }

    @Test
    fun text() {
        execute("Hello, world!") {
            assertEquals("<p>Hello, world!</p>", it)
        }

        execute("# Title\n Hello, world!\n## Subtitle\nHello, world!") {
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
                "<ul><li><input disabled=\"\" type=\"checkbox\" />Unchecked</li>" +
                    "<li><input disabled=\"\" type=\"checkbox\" checked=\"\" />Checked</li></ul>",
                it,
            )
        }
    }

    @Test
    fun code() {
        execute("`println(\"Hello, world!\")`") {
            assertEquals("<p><code>println(&quot;Hello, world!&quot;)</code></p>", it)
        }

        execute("```\nprintln(\"Hello, world!\")\n```") {
            assertEquals("<pre><code>println(&quot;Hello, world!&quot;)</code></pre>", it)
        }

        execute("```kotlin\nprintln(\"Hello, world!\")\n```") {
            assertEquals("<pre><code class=\"language-kotlin\">println(&quot;Hello, world!&quot;)</code></pre>", it)
        }

        execute("```kotlin\nfun hello() {\n    println(\"Hello, world!\")\n}\n```") {
            assertEquals(
                "<pre><code class=\"language-kotlin\">fun hello() {\n    println(&quot;Hello, world!&quot;)\n}</code></pre>",
                it,
            )
        }
    }

    @Test
    fun tables() {
        execute("| Header 1 | Header 2 |\n|----------|----------|\n| Cell 1   | Cell 2   |") {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody></table>",
                it,
            )
        }

        execute("| Header 1 | Header 2 | Header 3 |\n|:---------|:--------:|---------:|\n| Cell 1   | Cell 2   | Cell 3   |") {
            assertEquals(
                "<table><thead><tr><th align=\"left\">Header 1</th><th align=\"center\">Header 2</th>" +
                    "<th align=\"right\">Header 3</th></tr></thead><tbody><tr><td align=\"left\">Cell 1</td>" +
                    "<td align=\"center\">Cell 2</td><td align=\"right\">Cell 3</td></tr></tbody></table>",
                it,
            )
        }
    }

    @Test
    fun functions() {
        execute(".sum {3} {4}") {
            assertEquals("<p>7</p>", it)
        }

        execute(".multiply {3} by:{6}") {
            assertEquals("<p>18</p>", it)
        }

        // TODO add space after $ in the output (= avoid trimming text)
        execute("$ 4 - 2 = $ .subtract {4} {2}") {
            assertEquals("<p>__QD_INLINE_MATH__$4 - 2 =\$__QD_INLINE_MATH__2</p>", it)
        }
    }
}
