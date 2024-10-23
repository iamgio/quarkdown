package eu.iamgio.quarkdown.test

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.MutableContextOptions
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.document.page.PageOrientation
import eu.iamgio.quarkdown.document.page.PageSizeFormat
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.error.InvalidArgumentCountException
import eu.iamgio.quarkdown.function.error.InvalidFunctionCallException
import eu.iamgio.quarkdown.function.error.UnresolvedReferenceException
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineHooks
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.PipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import eu.iamgio.quarkdown.stdlib.Stdlib
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Default execution options.
private val DEFAULT_OPTIONS =
    MutableContextOptions(
        enableAutomaticIdentifiers = false,
        enableLocationAwareness = false,
    )

// Folder to retrieve test data from.
private const val DATA_FOLDER = "src/test/resources/data"

/**
 * Tests that cover the whole pipeline from lexing to rendering, including function call expansion.
 * [Stdlib] is used as a library.
 */
class FullPipelineTest {
    /**
     * Executes a Quarkdown source.
     * @param source Quarkdown source to execute
     * @param options execution options
     * @param errorHandler error handler to use
     * @param enableMediaStorage whether the media storage system should be enabled.
     * If enabled, nodes that reference media (e.g. images) will instead reference the path to the media on the local storage
     * @param hook action run after rendering. Parameters are the pipeline context and the rendered source
     */
    private fun execute(
        source: String,
        options: MutableContextOptions = DEFAULT_OPTIONS.copy(),
        errorHandler: PipelineErrorHandler = StrictPipelineErrorHandler(),
        enableMediaStorage: Boolean = false,
        hook: Context.(CharSequence) -> Unit,
    ) {
        val context =
            MutableContext(
                QuarkdownFlavor,
                options = options,
            )

        val hooks =
            PipelineHooks(
                afterRendering = { hook(context, it) },
            )

        val pipeline =
            Pipeline(
                context,
                PipelineOptions(
                    errorHandler = errorHandler,
                    workingDirectory = File(DATA_FOLDER),
                    enableMediaStorage = enableMediaStorage,
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
            assertEquals(DocumentType.PLAIN, documentInfo.type)
            assertNull(documentInfo.name)
            assertNull(documentInfo.author)
            assertNull(documentInfo.locale)
        }

        execute(
            """
            .docname {My Quarkdown document}
            .docauthor {iamgio}
            .doctype {slides}
            .doclang {english}
            .theme {darko} layout:{minimal}
            .pageformat {A3} orientation:{landscape} margin:{3cm 2px}
            .slides transition:{zoom} speed:{fast}
            .autopagebreak maxdepth:{3}
            """.trimIndent(),
        ) {
            assertEquals("My Quarkdown document", documentInfo.name)
            assertEquals("iamgio", documentInfo.author)
            assertEquals("en", documentInfo.locale?.tag)
            assertEquals(DocumentType.SLIDES, documentInfo.type)
            assertEquals("darko", documentInfo.theme?.color)
            assertEquals("minimal", documentInfo.theme?.layout)

            PageSizeFormat.A3.getBounds(PageOrientation.LANDSCAPE).let { bounds ->
                assertEquals(bounds.width, documentInfo.pageFormat.pageWidth)
                assertEquals(bounds.height, documentInfo.pageFormat.pageHeight)
            }

            assertEquals(
                Sizes(
                    vertical = Size(3.0, Size.Unit.CM),
                    horizontal = Size(2.0, Size.Unit.PX),
                ),
                documentInfo.pageFormat.margin,
            )
        }
    }

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
            assertEquals("<p>A</p><div class=\"align align-end\"><h3>B</h3></div><p>C</p>", it)
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
    }

    @Test
    fun images() {
        execute("![Alt text](https://example.com/image.png)") {
            assertEquals("<p><img src=\"https://example.com/image.png\" alt=\"Alt text\" /></p>", it)
        }

        execute("![Alt text](https://example.com/image.png 'Title')") {
            assertEquals(
                "<p><figure>" +
                    "<img src=\"https://example.com/image.png\" alt=\"Alt text\" title=\"Title\" />" +
                    "<figcaption>Title</figcaption>" +
                    "</figure></p>",
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
                "<ul><li><input disabled=\"\" type=\"checkbox\" />Unchecked</li>" +
                    "<li><input disabled=\"\" type=\"checkbox\" checked=\"\" />Checked</li></ul>",
                it,
            )
        }
    }

    @Test
    fun code() {
        execute("`println(\"Hello, world!\")`") {
            assertEquals(
                "<p><span class=\"codespan-content\"><code>println(&quot;Hello, world!&quot;)</code></span></p>",
                it,
            )
            assertFalse(attributes.hasCode)
        }

        // Color preview
        execute("`#FF0000`") {
            assertEquals(
                "<p>" +
                    "<span class=\"codespan-content\">" +
                    "<code>#FF0000</code>" +
                    "<span style=\"background-color: rgba(255, 0, 0, 1.0);\" class=\"color-preview\"></span>" +
                    "</span>" +
                    "</p>",
                it,
            )
            assertFalse(attributes.hasCode)
        }

        execute("`rgba(200, 100, 50, 0.5)`") {
            assertEquals(
                "<p>" +
                    "<span class=\"codespan-content\">" +
                    "<code>rgba(200, 100, 50, 0.5)</code>" +
                    "<span style=\"background-color: rgba(200, 100, 50, 0.5);\" class=\"color-preview\"></span>" +
                    "</span>" +
                    "</p>",
                it,
            )
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

        execute("| Header 1 | Header 2 |\n|----------|----------|\n| $ X $ | $ Y $ |") {
            assertEquals(
                "<table><thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>" +
                    "<tbody><tr><td>__QD_INLINE_MATH__\$X\$__QD_INLINE_MATH__</td>" +
                    "<td>__QD_INLINE_MATH__\$Y\$__QD_INLINE_MATH__</td></tr></tbody></table>",
                it,
            )
            assertTrue(attributes.hasMath) // Ensures the tree traversal visits table cells too.
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

        execute(
            """
            .divide {
              .cos {.pi}
            } by:{
              .sin {
                1
              }
            }
            """.trimIndent(),
        ) {
            assertEquals("<p>-1.1883951</p>", it)
        }

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
                    "<h2>Title 2</h2><h2>Title 2</h2><div class=\"page-break\" data-hidden=\"\"></div><h1>Title 1</h1>".repeat(
                        2,
                    ) +
                        "<p>Some text</p>"
                ).repeat(2) + "<h3>Title 3</h3>",
                it,
            )
        }

        execute(".function {hello}\n  *Hello*!\n\n.hello") {
            assertEquals("<p><em>Hello</em>!</p>", it)
        }

        execute(".function {hello}\n   target:\n  **Hello** .target!\n\n.hello {world}") {
            assertEquals("<p><strong>Hello</strong> world!</p>", it)
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
            assertEquals("<pre><code>Line 1${System.lineSeparator()}Line 2</code></pre>", it)
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
    fun iterables() {
        execute(
            """
            .var {x}
              - A
              - B
              - C

            .foreach {.x}
              .1
            """.trimIndent(),
        ) {
            assertEquals("<p>A</p><p>B</p><p>C</p>", it)
        }

        execute(
            """
            .var {x}
              - A
              - B
              - C

            .foreach {.x}
              .lowercase {.1}
            """.trimIndent(),
        ) {
            assertEquals("<p>a</p><p>b</p><p>c</p>", it)
        }

        execute(
            """
            .var {nums}
              - 1
              - 2
              - 3
              - 4

            .foreach {.nums}
              n:
              .pow {.n} to:{2}
            """.trimIndent(),
        ) {
            assertEquals("<p>1</p><p>4</p><p>9</p><p>16</p>", it)
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
                "<div style=\"justify-content: flex-start; align-items: center;\" class=\"stack stack-row\">" +
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
                    
                .box {Hello 4} type:{tip}
                    Hello 5
            """.trimIndent(),
        ) {
            assertEquals(
                "<div style=\"justify-content: space-between; align-items: flex-start; gap: 1.0cm;\" class=\"stack stack-column\">" +
                    "<p>Hello 1</p>" +
                    "<h2>Hello 2</h2>" +
                    "<pre><code>Hello 3</code></pre>" +
                    "<div class=\"box tip\"><header><h4>Hello 4</h4></header><div class=\"box-content\"><p>Hello 5</p></div></div>" +
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
                "<div style=\"justify-content: center; align-items: center; gap: 200.0px;\" class=\"stack stack-row\">" +
                    "<div style=\"justify-content: flex-start; align-items: flex-end;\" class=\"stack stack-column\">" +
                    "<h2>Quarkdown</h2><p>A cool language</p>" +
                    "</div>" +
                    "<div style=\"justify-content: flex-start; align-items: center; gap: 1.0cm;\" class=\"stack stack-column\">" +
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
    fun boxes() {
        execute(".box {Hello} \n\tHello, **world**!") {
            assertEquals(
                "<div class=\"box callout\"><header><h4>Hello</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, <strong>world</strong>!</p></div></div>",
                it,
            )
        }

        execute(".box {Hello} type:{tip}\n\tHello, world!") {
            assertEquals(
                "<div class=\"box tip\"><header><h4>Hello</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        execute(".box {Hello, *world*} type:{warning}\n\tHello, world!") {
            assertEquals(
                "<div class=\"box warning\"><header><h4>Hello, <em>world</em></h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        execute(".box type:{error}\n\tHello, world!") {
            assertEquals(
                "<div class=\"box error\">" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        // Localized title.
        execute(
            """
            .doclang {english}
            .box type:{error}
              Hello, world!
            
            .box type:{tip}
               Hello, world!
               
            .box
              Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"box error\"><header><h4>Error</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>" +
                    "<div class=\"box tip\"><header><h4>Tip</h4></header>" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>" +
                    "<div class=\"box callout\">" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
                it,
            )
        }

        // Unsupported localization.
        execute(
            """
            .doclang {japanese}
            .box type:{warning}
              Hello, world!
            """.trimIndent(),
        ) {
            assertEquals(
                "<div class=\"box warning\">" +
                    "<div class=\"box-content\"><p>Hello, world!</p></div></div>",
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

        val iterativeInFunction =
            """
            .function {fib}
                n:
                .var {t1} {0}
                .var {t2} {1}
                
                .table
                    .repeat {.n}
                        | $ F_{.subtract {.1} {1}} $ |
                        |:--------------------------:|
                        |             .t1            |
                        .var {tmp} {.sum {.t1} {.t2}}
                        .var {t1} {.t2}
                        .var {t2} {.tmp}

            .fib {5}
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

        execute(iterativeInFunction) {
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

        execute(
            """
            .function {poweredby}
                credits:
                .text {powered by .credits} size:{small} variant:{smallcaps}
            
            This **exciting feature**, .poweredby {[Quarkdown](https://github.com/iamgio/quarkdown)}, looks great!
            """.trimIndent(),
        ) {
            assertEquals(
                "<p>This <strong>exciting feature</strong>, <span class=\"size-small\" style=\"font-variant: small-caps;\">" +
                    "powered by <a href=\"https://github.com/iamgio/quarkdown\">Quarkdown</a></span>, looks great!</p>",
                it,
            )
        }
    }

    @Test
    fun numbering() {
        // Numbering is disabled by default.
        execute(
            """
            .noautopagebreak
            # A
            ## A/1
            # B
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>A</h1>" +
                    "<h2>A/1</h2>" +
                    "<h1>B</h1>",
                it,
            )
        }

        execute(
            """
            .noautopagebreak
            .numbering {1.1}
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
            .numbering {1.1}
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

        execute(
            """
            .noautopagebreak
            .numbering {A.a.1}
            # A
            ## A/1
            ### A/1/1
            ## A/2
            # B
            ### B/1/1
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
                    "<h3 data-location=\"B.a.1\">B/1/1</h3>" +
                    "<h1 data-location=\"C\">C</h1>" +
                    "<h2 data-location=\"C.a\">C/1</h2>" +
                    "<h3 data-location=\"C.a.1\">C/1/1</h3>" +
                    "<h2 data-location=\"C.b\">C/2</h2>",
                it,
            )
        }

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
            .numbering {none}
            .noautopagebreak
            # A
            ## A/1
            # B
            """.trimIndent(),
            DEFAULT_OPTIONS.copy(enableLocationAwareness = true),
        ) {
            assertEquals(
                "<h1>A</h1>" +
                    "<h2>A/1</h2>" +
                    "<h1>B</h1>",
                it,
            )
        }
    }

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
                    "<ol><li><a href=\"#abc1\"><em>ABC/1</em></a></li></ol></li>" +
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

        // Markers
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
                    "<li><a href=\"#marker-1\"><em>Marker 1</em></a></li>" +
                    "<li><a href=\"#marker-2\"><em>Marker 2</em></a></li>" +
                    "</ol></nav>" +
                    "<div class=\"marker\" data-hidden=\"\" id=\"marker-1\"></div>" +
                    "<div class=\"page-break\" data-hidden=\"\"></div>" +
                    "<h1 id=\"abc\">ABC</h1>" +
                    "<div class=\"marker\" data-hidden=\"\" id=\"marker-2\"></div>" +
                    "<h2 id=\"def\">DEF</h2>",
                it,
            )
        }

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

        // Numbering
        execute(
            """
            .numbering {1.A.a}
            .noautopagebreak
            .tableofcontents title:{TOC}
            
            # A            
            ## A/1
            ### A/1/1
            ## A/2
            # B
            ### B/1/1
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
                    "<ol><li data-location=\"2.A.a\"><a href=\"#b11\">B/1/1</a></li></ol></li>" +
                    "</ol></nav>" +
                    "<h1 id=\"a\" data-location=\"1\">A</h1>" +
                    "<h2 id=\"a1\" data-location=\"1.A\">A/1</h2>" +
                    "<h3 id=\"a11\" data-location=\"1.A.a\">A/1/1</h3>" +
                    "<h2 id=\"a2\" data-location=\"1.B\">A/2</h2>" +
                    "<h1 id=\"b\" data-location=\"2\">B</h1>" +
                    "<h3 id=\"b11\" data-location=\"2.A.a\">B/1/1</h3>",
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

        // Included file cannot be used as a dynamic value.
        assertFails {
            execute(
                """
                .sum {.include {include/include-6.md}} {3}
                """.trimIndent(),
            ) {}
        }
    }

    @Test
    fun localization() {
        execute(
            """
            .doclang {english}
            .localization {mytable}
                - English
                    - morning: Good morning
                    - evening: Good evening
                - Italian
                    - morning: Buongiorno
                    - evening: Buonasera
            
            > .localize {mytable:morning}.
            """.trimIndent(),
        ) {
            assertEquals("<blockquote><p>Good morning.</p></blockquote>", it)
        }

        execute(
            """
            .doclang {italian}
            .localization {mytable}
                - English
                    - theorem: Theorem
                - Italian
                    - theorem: Teorema

            .function {theorem}
                **.localize {mytable:theorem}.**

            .theorem Test
            """.trimIndent(),
        ) {
            assertEquals("<p><strong>Teorema.</strong> Test</p>", it)
        }

        assertFails {
            execute(
                """
                .doclang {english}
                .localization {mytable}
                    - English
                        - morning: Good morning
                        - evening: Good evening
                    - Italian
                        - morning: Buongiorno
                        - evening: Buonasera
                
                > .localize {mytable:afternoon}.
                """.trimIndent(),
            ) {}
        }

        // Library's injected localization table.
        execute(
            """
            .doclang {english}
            .localize {std:warning}
            """.trimIndent(),
        ) {
            assertEquals("<p>Warning</p>", it)
        }
    }

    @Test
    fun `media storage`() {
        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = false,
        ) {
            assertEquals("<p>This is the Quarkdown logo: <img src=\"img/icon.png\" alt=\"Quarkdown\" />.</p>", it)
            assertEquals(0, mediaStorage.all.size)
        }

        execute(
            """
            This is the Quarkdown logo: ![Quarkdown](img/icon.png).                                 
            """.trimIndent(),
            enableMediaStorage = true,
        ) {
            assertEquals("<p>This is the Quarkdown logo: <img src=\"media/icon", it.toString().substringBefore("@"))
            // The file name is "media/icon-[encoded].png"
            assertEquals("\" alt=\"Quarkdown\" />.</p>", it.toString().substringAfter(".png"))
        }

        execute(
            """
            .center
                ![Icon](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/ticon-light.svg "The Quarkdown icon")
                
                ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
        ) {
            assertEquals(
                "<div class=\"align align-center\">" +
                    "<p>" +
                    "<figure>" +
                    "<img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-ticon-light.svg\" " +
                    "alt=\"Icon\" title=\"The Quarkdown icon\" />" +
                    "<figcaption>The Quarkdown icon</figcaption>" +
                    "</figure>" +
                    "</p>" +
                    "<p>" +
                    "<img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-tbanner-light.svg\" " +
                    "alt=\"Banner\" />" +
                    "</p>" +
                    "</div>",
                it,
            )

            assertEquals(2, mediaStorage.all.size)
        }

        execute(
            """
            ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)  
            ![Quarkdown](img/icon.png)
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = false),
            enableMediaStorage = true,
        ) {
            assertEquals(
                "<p>" +
                    "<img src=\"https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg\" " +
                    "alt=\"Banner\" /><br /><img src=\"media/",
                it.toString().substringBefore("icon@"),
            )

            assertEquals(1, mediaStorage.all.size)
        }

        execute(
            """
            [Banner]: https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg
            ![Banner]
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
        ) {
            assertEquals(
                "<p><img src=\"media/https-raw.githubusercontent.com-iamgio-quarkdown-project-files-images-tbanner-light.svg\" " +
                    "alt=\"Banner\" /></p>",
                it,
            )
        }
    }

    @Test
    fun errors() {
        assertFailsWith<UnresolvedReferenceException> {
            execute(".nonexistant") {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2}") {}
        }

        assertFailsWith<InvalidArgumentCountException> {
            execute(".sum {2} {5} {9}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".sum {a} {3}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".if {hello}\n\t.sum {2} {3} {1}") {}
        }

        assertFailsWith<InvalidFunctionCallException> {
            execute(".row alignment:{center} cross:{hello}\n\tHi") {}
        }

        // Non-strict error handling.

        execute(".sum {a} {3}", errorHandler = BasePipelineErrorHandler()) {
            assertEquals(
                "<div class=\"box error\">" +
                    "<header><h4>Error: sum</h4></header>" +
                    "<div class=\"box-content\"><p>" +
                    "Cannot call function sum" +
                    "<span class=\"inline-collapse\" data-full-text=\"(Number a, Number b)\" " +
                    "data-collapsed-text=\"(...)\" data-collapsed=\"false\">" +
                    "(Number a, Number b)" +
                    "</span>" +
                    " with arguments " +
                    "<span class=\"inline-collapse\" data-full-text=\"(a, 3)\" data-collapsed-text=\"(...)\" data-collapsed=\"false\">" +
                    "(a, 3)" +
                    "</span>: <br />" +
                    "<em>Not a numeric value: a</em>" +
                    "</p></div></div>",
                it,
            )
        }

        execute(".if {yes}\n\t.sum {a} {3}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: sum</h4>")
        }

        execute(".if {yes}\n\t.row\n\t\t.sum {2} {1} {5}", errorHandler = BasePipelineErrorHandler()) {
            assertContains(it, "<h4>Error: sum</h4>")
        }

        execute(".if {yes}\n\t.column alignment:{x}\n\t\tHi", errorHandler = BasePipelineErrorHandler()) {
            assertTrue(
                Regex(
                    ".+?<header><h4>Error: column</h4></header>" +
                        ".+?<p>" +
                        "Cannot call function column.+?No such element 'x' among values \\[.+?]" +
                        "</p>.+",
                ).matches(it),
            )
        }
    }
}
