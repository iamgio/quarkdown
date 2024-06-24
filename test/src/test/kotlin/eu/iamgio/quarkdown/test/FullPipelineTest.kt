package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.SystemProperties
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.stdlib.Stdlib
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val DATA_FOLDER = "src/test/resources/data"

/**
 * Tests that cover the whole pipeline from lexing to rendering, including function call expansion.
 * [Stdlib] is used as a library.
 */
class FullPipelineTest {
    @BeforeTest
    fun setup() {
        SystemProperties[SystemProperties.EXIT_ON_ERROR] = ""
    }

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

        execute("***result***: .sum {3} {.multiply {4} {2}}") {
            assertEquals("<p><em><strong>result</strong></em>: 11</p>", it)
        }

        execute(".code\n    .filecontent {$DATA_FOLDER/code.txt}") {
            assertEquals(
                "<pre><code>Line 1${System.lineSeparator()}Line 2${System.lineSeparator()}${System.lineSeparator()}Line 3</code></pre>",
                it,
            )
        }
    }

    @Test
    fun `flow functions`() {
        execute(".if { .islower {2} than:{3} }\n  **Text**") {
            assertEquals("<p><strong>Text</strong></p>", it)
        }

        execute(".if { .islower {3} than:{2} }\n  **Text**") {
            assertEquals("", it)
        }

        execute(".ifnot { .islower {3} than:{2} }\n  **Text**") {
            assertEquals("<p><strong>Text</strong></p>", it)
        }

        execute(".foreach {..3}\n  **N:** .1") {
            assertEquals("<p><strong>N:</strong> 0</p><p><strong>N:</strong> 1</p><p><strong>N:</strong> 2</p>", it)
        }

        execute(
            """
            # Title
            .foreach {..2}
                n:
                Hi .n
            """.trimIndent(),
        ) {
            assertEquals("<h1>Title</h1><p>Hi 0</p><p>Hi 1</p>", it)
        }

        execute(
            """
            .foreach {..2}
                # Hello
                .foreach {..1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals("<h1>Hello</h1><p><strong>Hi</strong>!</p><h1>Hello</h1><p><strong>Hi</strong>!</p>", it)
        }

        execute(
            """
            .foreach {..2}
                .foreach {..2}
                    .foreach {..2}
                        ## Title 2
                    # Title 1
            
                Some text
            ### Title 3
            """.trimIndent(),
        ) {
            assertEquals(
                (
                    "<h2>Title 2</h2><h2>Title 2</h2><h1>Title 1</h1>".repeat(2) +
                        "<p>Some text</p>"
                ).repeat(2) + "<h3>Title 3</h3>",
                it,
            )
        }

        execute(".function {hello}\n  `Hello`!\n\n.hello") {
            assertEquals("<p><code>Hello</code>!</p>", it)
        }

        execute(".function {hello}\n   target:\n  `Hello` .target!\n\n.hello {world}") {
            assertEquals("<p><code>Hello</code> world!</p>", it)
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".function {hello}\n   target:\n  `Hello` .target!\n\n.hello") {}
        }
    }

    @Test
    fun `type inference`() {
        execute(
            """
            .function {x}
                arg:
                .if {.arg}
                    Hi
                .ifnot {.arg}
                    Hello

            .x {no}
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello</p>", it)
        }
    }

    @Test
    fun stacks() {
        execute(
            """
            .row
                Hello 1
                Hello 2
                
                Hello 3
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: flex-start; align-items: center;\" class=\"stack stack-horizontal\">" +
                    "<p>Hello 1\nHello 2</p><p>Hello 3</p>" +
                    "</div>",
                it,
            )
        }

        execute(
            """
            .column alignment:{spacebetween} cross:{start} gap:{1cm}
                Hello 1
                
                ## Hello 2
                
                    Hello 3
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: space-between; align-items: flex-start; gap: 1.0cm;\" class=\"stack stack-vertical\">" +
                    "<p>Hello 1</p>" +
                    "<h2>Hello 2</h2>" +
                    "<pre><code>Hello 3</code></pre>" +
                    "</div>",
                it,
            )
        }

        execute(
            """
            .row alignment:{center} cross:{center} gap:{200px}
                .column cross:{end}
                    # Quarkdown
                    A cool language

                .column gap:{1cm}
                    .clip {circle}
                        ![](img1.png)

                    .clip {circle}
                        ![](img2.png)

                    .clip {circle}
                        ![](img3.png)

                **[GitHub](https://github.com/iamgio/quarkdown)**
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: center; align-items: center; gap: 200.0px;\" class=\"stack stack-horizontal\">" +
                    "<div style=\"justify-content: flex-start; align-items: flex-end;\" class=\"stack stack-vertical\">" +
                    "<h1>Quarkdown</h1><p>A cool language</p>" +
                    "</div>" +
                    "<div style=\"justify-content: flex-start; align-items: center; gap: 1.0cm;\" class=\"stack stack-vertical\">" +
                    "<div class=\"clip-circle\">" +
                    "<p><img src=\"img1.png\" alt=\"\" /></p>" +
                    "</div>" +
                    "<div class=\"clip-circle\">" +
                    "<p><img src=\"img2.png\" alt=\"\" /></p>" +
                    "</div>" +
                    "<div class=\"clip-circle\">" +
                    "<p><img src=\"img3.png\" alt=\"\" /></p>" +
                    "</div>" +
                    "</div>" +
                    "<p><strong><a href=\"https://github.com/iamgio/quarkdown\">GitHub</a></strong></p>" +
                    "</div>",
                it,
            )
        }
    }

    @Test
    fun fibonacci() {
        // Iterative Fibonacci sequence calculation.
        val iterative =
            """
            .var {t1} {0}
            .var {t2} {1}
            
            .table
                .foreach {..4}
                    | $ F_{.1} $ |
                    |:-------------:|
                    |      .t1      |
                    .var {tmp} {.sum {.t1} {.t2}}
                    .var {t1} {.t2}
                    .var {t2} {.tmp}
            """.trimIndent()

        val alternativeIterative =
            """
            .var {t1} {0}
            .var {t2} {1}
            
            .function {tablecolumn}
                n:
                |  $ F_{.n} $  |
                |:-------------:|
                |      .t1      |
            
            .table
                .foreach {..4}
                    .tablecolumn {.1}
                    .var {tmp} {.sum {.t1} {.t2}}
                    .var {t1} {.t2}
                    .var {t2} {.tmp}
            """.trimIndent()

        // Recursive Fibonacci sequence calculation.
        val recursive =
            """
            .function {fib}
                n:
                .if { .islower {.n} than:{2} }
                    .n
                .ifnot { .islower {.n} than:{2} }
                    .sum {
                        .fib { .subtract {.n} {1} }
                    } {
                        .fib { .subtract {.n} {2} }
                    }
              
            .table
                .foreach {..4}
                    | $ F_{.1} $  |
                    |:------------:|
                    | .fib {.1} |
            """.trimIndent()

        val out =
            "<table><thead><tr>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{0}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{1}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{2}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{3}\$__QD_INLINE_MATH__</th></tr></thead><tbody><tr>" +
                "<td align=\"center\">0</td><td align=\"center\">1</td>" +
                "<td align=\"center\">1</td>" +
                "<td align=\"center\">2</td></tr></tbody></table>"

        execute(iterative) {
            assertEquals(out, it)
        }

        execute(alternativeIterative) {
            assertEquals(out, it)
        }

        execute(recursive) {
            assertEquals(out, it)
        }
    }

    @Test
    fun `layout builder`() {
        val layoutFunction =
            """
            .function {mylayout}
                name number:
                # Hello, .name!
                
                .number $ \times $ .number is .multiply {.number} by:{.number}
                
                ### End
                
            """.trimIndent()

        execute(
            "$layoutFunction\n.mylayout {world} {3}",
        ) {
            assertEquals(
                "<h1>Hello, world!</h1><p>3__QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__3 is 9</p><h3>End</h3>",
                it,
            )
        }

        execute(
            layoutFunction +
                """
                    
                .foreach {..4}
                    n:
                    .mylayout {world} {.n}
                """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Hello, world!</h1><p>0__QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__0 is 0</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>1__QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__1 is 1</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>2__QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__2 is 4</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>3__QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__3 is 9</p><h3>End</h3>",
                it,
            )
        }
    }
}
