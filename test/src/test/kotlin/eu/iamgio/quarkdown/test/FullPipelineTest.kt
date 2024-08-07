package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.MutableContextOptions
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import eu.iamgio.quarkdown.stdlib.Stdlib
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val DATA_FOLDER = "src/test/resources/data"

/**
 * Tests that cover the whole pipeline from lexing to rendering, including function call expansion.
 * [Stdlib] is used as a library.
 */
class FullPipelineTest {
    /**
     * Executes a Quarkdown source.
     * @param hook action run after rendering. Parameters are the pipeline context and the rendered source
     */
    private fun execute(
        source: String,
        hook: Context.(CharSequence) -> Unit,
    ) {
        val context =
            MutableContext(
                QuarkdownFlavor,
                options = MutableContextOptions(enableAutomaticIdentifiers = false),
            )

        val hooks =
            PipelineHooks(
                afterRendering = { hook(context, it) },
            )

        val pipeline =
            Pipeline(
                context,
                PipelineOptions(
                    errorHandler = StrictPipelineErrorHandler(),
                    workingDirectory = File(DATA_FOLDER),
                ),
                libraries = setOf(Stdlib.library),
                renderer = { rendererFactory, ctx -> rendererFactory.html(ctx) },
                hooks,
            )

        pipeline.execute(source)
    }

    @Test
    fun document() {
        execute("") {
            assertEquals("", it)
            assertIs<AstRoot>(attributes.root)
            assertFalse(attributes.hasCode)
            assertFalse(attributes.hasMath)
            assertTrue(attributes.linkDefinitions.isEmpty())
        }
    }

    @Test
    fun text() {
        execute("Hello, world!") {
            assertEquals("<p>Hello, world!</p>", it)
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
            assertFalse(attributes.hasCode)
        }

        execute("```\nprintln(\"Hello, world!\")\n```") {
            assertEquals("<pre><code>println(&quot;Hello, world!&quot;)</code></pre>", it)
            assertTrue(attributes.hasCode)
        }

        execute("```kotlin\nprintln(\"Hello, world!\")\n```") {
            assertEquals("<pre><code class=\"language-kotlin\">println(&quot;Hello, world!&quot;)</code></pre>", it)
            assertTrue(attributes.hasCode)
        }

        execute("```kotlin\nfun hello() {\n    println(\"Hello, world!\")\n}\n```") {
            assertEquals(
                "<pre><code class=\"language-kotlin\">fun hello() {\n    println(&quot;Hello, world!&quot;)\n}</code></pre>",
                it,
            )
            assertTrue(attributes.hasCode)
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
            assertEquals("<p>__QD_INLINE_MATH__$4 - 2 =\$__QD_INLINE_MATH__ 2</p>", it)
            assertTrue(attributes.hasMath)
        }

        execute("***result***: .sum {3} {.multiply {4} {2}}") {
            assertEquals("<p><em><strong>result</strong></em>: 11</p>", it)
            assertFalse(attributes.hasMath)
        }

        execute(".code\n    .read {code.txt}") {
            assertEquals(
                "<pre><code>Line 1${System.lineSeparator()}Line 2${System.lineSeparator()}${System.lineSeparator()}Line 3</code></pre>",
                it,
            )
            assertTrue(attributes.hasCode)
        }
    }

    @Test
    fun `node mapping`() {
        // Function is a block
        execute(
            """
            ## Title
            
            .libexists {stdlib}
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p><input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>", it)
        }

        // Function is inline
        execute(
            """
            ## Title
            
            Text .libexists {stdlib}
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p>Text <input disabled=\"\" type=\"checkbox\" checked=\"\" /></p>", it)
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
            assertEquals("<p><strong>N:</strong> 1</p><p><strong>N:</strong> 2</p><p><strong>N:</strong> 3</p>", it)
        }

        execute(
            """
            ## Title
            .foreach {..2}
                n:
                Hi .n
            """.trimIndent(),
        ) {
            assertEquals("<h2>Title</h2><p>Hi 1</p><p>Hi 2</p>", it)
        }

        execute(
            """
            .foreach {..2}
                ## Hello .1
                .foreach {..1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals("<h2>Hello 1</h2><p><strong>Hi</strong>!</p><h2>Hello 2</h2><p><strong>Hi</strong>!</p>", it)
        }

        execute(
            """
            .repeat {2}
                ## Hello .1
                .repeat {1}
                    **Hi**!
            """.trimIndent(),
        ) {
            assertEquals("<h2>Hello 1</h2><p><strong>Hi</strong>!</p><h2>Hello 2</h2><p><strong>Hi</strong>!</p>", it)
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
                    "<h2>Title 2</h2><h2>Title 2</h2><div class=\"page-break\"></div><h1>Title 1</h1>".repeat(2) +
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

        execute(
            """
            .if {yes}
                .function {hello}
                    name:
                    Hello, *.name*!
                
                #### .hello {world}
                .hello {iamgio}
            """.trimIndent(),
        ) {
            assertEquals("<h4>Hello, <em>world</em>!</h4><p>Hello, <em>iamgio</em>!</p>", it)
        }

        execute(
            """
            .let {world}
                Hello, **.1**!
            """.trimIndent(),
        ) {
            assertEquals("<p>Hello, <strong>world</strong>!</p>", it)
        }

        execute(
            """
            .foreach {..3}
                .let {.1}
                    x:
                    .sum {3} {.x}
            """.trimIndent(),
        ) {
            assertEquals("<p>4</p><p>5</p><p>6</p>", it)
        }

        execute(
            """
            .let {code.txt}
                file:
                .let {.read {.file} {..2}}
                    .code
                        .1
            """.trimIndent(),
        ) {
            assertEquals("<pre><code>Line 1\nLine 2</code></pre>", it)
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

        execute(
            """
            .var {x} {no}
            .if {.x}
                Hi
            .ifnot {.x}
                Hello
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
                    ## Quarkdown
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
                    "<h2>Quarkdown</h2><p>A cool language</p>" +
                    "</div>" +
                    "<div style=\"justify-content: flex-start; align-items: center; gap: 1.0cm;\" class=\"stack stack-vertical\">" +
                    "<div class=\"clip clip-circle\">" +
                    "<p><img src=\"img1.png\" alt=\"\" /></p>" +
                    "</div>" +
                    "<div class=\"clip clip-circle\">" +
                    "<p><img src=\"img2.png\" alt=\"\" /></p>" +
                    "</div>" +
                    "<div class=\"clip clip-circle\">" +
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
                .foreach {0..4}
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
                .foreach {0..4}
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
                .foreach {0..4}
                    | $ F_{.1} $  |
                    |:------------:|
                    | .fib {.1} |
            """.trimIndent()

        val out =
            "<table><thead><tr>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{0}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{1}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{2}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{3}\$__QD_INLINE_MATH__</th>" +
                "<th align=\"center\">__QD_INLINE_MATH__\$F_{4}\$__QD_INLINE_MATH__</th>" +
                "</tr></thead><tbody><tr>" +
                "<td align=\"center\">0</td>" +
                "<td align=\"center\">1</td>" +
                "<td align=\"center\">1</td>" +
                "<td align=\"center\">2</td>" +
                "<td align=\"center\">3</td>" +
                "</tr></tbody></table>"

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
            .noautopagebreak
            
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
                "<h1>Hello, world!</h1><p>3 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 3 is 9</p><h3>End</h3>",
                it,
            )
        }

        execute(
            layoutFunction +
                """
                    
                .repeat {4}
                    n:
                    .mylayout {world} {.n}
                """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Hello, world!</h1><p>1 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 1 is 1</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>2 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 2 is 4</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>3 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 3 is 9</p><h3>End</h3>" +
                    "<h1>Hello, world!</h1><p>4 __QD_INLINE_MATH__$\\times\$__QD_INLINE_MATH__ 4 is 16</p><h3>End</h3>",
                it,
            )
        }
    }

    @Test
    fun `include source`() {
        execute(
            """
            .noautopagebreak
            .include {include/include-1.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Title</h1><p>Some <em>text</em>.</p>",
                it,
            )
        }

        // Import functions from another source.
        execute(
            """
            .include {include/include-2.md}
            .hello {world}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>Hello, world!</p>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            # Main
            .include {include/include-3.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Main</h1><h2>Included</h2><pre><code>code\ncode</code></pre>",
                it,
            )
        }

        // Sharing functions with included files.
        execute(
            """
            .function {hello}
                x:
                Hello, .x!
            .include {include/include-4.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h3>Hello, world!</h3>",
                it,
            )
        }

        // Transitive inclusion of files.
        execute(
            """
            .noautopagebreak
            # Main
            .include {include/include-5.md}
            """.trimIndent(),
        ) {
            assertEquals(
                "<h1>Main</h1><h1>Included</h1><p>Hello, Gio!</p><h3>Hello, world!</h3>",
                it,
            )
        }

        // Included file used as a value.
        execute(
            """
            .sum {.include {include/include-6.md}} {3}
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>8</p>",
                it,
            )
        }
    }
}
