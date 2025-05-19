package com.quarkdown.rendering.html

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.document.size.inch
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.core.util.normalizeLineSeparators
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * HTML post renderer tests.
 */
class HtmlPostRendererTest {
    private lateinit var context: MutableContext

    @BeforeTest
    fun setup() {
        context = MutableContext(QuarkdownFlavor)
    }

    @Test
    fun empty() {
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor("<html><head></head><body></body></html>")
            }
        assertEquals(
            "<html><head></head><body></body></html>",
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `with content, single line`() {
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor("<html><head></head><body>[[CONTENT]]</body></html>")
                    .content("<strong>Hello, world!</strong>")
            }

        assertEquals(
            "<html><head></head><body><strong>Hello, world!</strong></body></html>",
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `with content, multiline`() {
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor(
                    """
                    <body>
                        [[CONTENT]]
                    </body>
                    """.trimIndent(),
                ).content("<strong>Hello, world!</strong>")
            }
        assertEquals(
            """
            <body>
                <strong>Hello, world!</strong>
            </body>
            """.trimIndent(),
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `with title`() {
        context.documentInfo.name = "Doc title"
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor("<head><title>[[TITLE]]</title></head><body>[[CONTENT]]</body>")
                    .content("<strong>Hello, world!</strong>")
            }
        assertEquals(
            "<head><title>Doc title</title></head><body><strong>Hello, world!</strong></body>",
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `math conditional`() {
        context.attributes.hasMath = true
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor("<body>[[if:MATH]][[CONTENT]][[endif:MATH]]</body>")
                    .content("<em>Hello, world!</em>")
            }
        assertEquals(
            "<body><em>Hello, world!</em></body>",
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `code conditional`() {
        context.attributes.hasCode = false
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor(
                    """
                    <body>
                        [[if:!CODE]]
                        [[CONTENT]]
                        [[endif:!CODE]]
                    </body>
                    """.trimIndent(),
                ).content("<em>Hello, world!</em>")
            }
        assertEquals(
            "<body>\n    <em>Hello, world!</em>\n</body>",
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `slides conditional`() {
        context.documentInfo.type = DocumentType.PLAIN
        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor(
                    """
                    <body>
                        [[if:SLIDES]]
                        <em>Hello, world!</em>
                        [[endif:SLIDES]]
                        [[if:!SLIDES]]
                        <strong>Hello, world!</strong>
                        [[endif:!SLIDES]]
                    </body>
                    """.trimIndent(),
                ).content("Hello, world!")
            }
        assertEquals(
            """
            <body>
                <strong>Hello, world!</strong>
            </body>
            """.trimIndent(),
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `semi-real`() {
        context.documentInfo.name = "Quarkdown"
        context.documentInfo.locale = LocaleLoader.SYSTEM.fromName("english")
        context.documentInfo.type = DocumentType.SLIDES
        context.attributes.hasMath = true
        context.attributes.hasCode = false

        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor(
                    """
                    <html[[if:LANG]] lang="[[LANG]]"[[endif:LANG]]>
                    <head>
                        [[if:SLIDES]]
                        <link rel="stylesheet" href="...css"></link>
                        [[endif:SLIDES]]
                        [[if:CODE]]
                        <script src="...js"></script>
                        <script src="...js"></script>
                        [[endif:CODE]]
                        [[if:MATH]]
                        <script src="...js"></script>
                        <script src="...js"></script>
                        [[endif:MATH]]
                        <title>[[TITLE]]</title>
                        <style>
                        [[if:MATH]]
                        mjx-container {
                            margin: 0 0.3em;
                        }
                        [[endif:MATH]]
                        </style>
                    </head>
                    <body>
                        [[if:SLIDES]]
                        <div class="reveal">
                            <div class="slides">
                                [[CONTENT]]
                            </div>
                        </div>
                        <script src="slides.js"></script>
                        [[endif:SLIDES]]
                        [[if:!SLIDES]]
                        [[CONTENT]]
                        [[endif:!SLIDES]]
                    </body>
                    </html>
                    """.trimIndent(),
                ).content("<p><em>Hello, world!</em></p>")
            }

        assertEquals(
            """
            <html lang="en">
            <head>
                <link rel="stylesheet" href="...css"></link>
                <script src="...js"></script>
                <script src="...js"></script>
                <title>Quarkdown</title>
                <style>
                mjx-container {
                    margin: 0 0.3em;
                }
                </style>
            </head>
            <body>
                <div class="reveal">
                    <div class="slides">
                        <p><em>Hello, world!</em></p>
                    </div>
                </div>
                <script src="slides.js"></script>
            </body>
            </html>
            """.trimIndent(),
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun real() {
        context.documentInfo.name = "Quarkdown"
        context.documentInfo.locale = LocaleLoader.SYSTEM.fromName("english")
        context.documentInfo.type = DocumentType.SLIDES
        context.attributes.hasMath = true
        context.attributes.hasCode = false
        context.documentInfo.pageFormat.pageWidth = 8.5.inch
        context.documentInfo.pageFormat.pageHeight = 11.0.inch
        context.documentInfo.pageFormat.margin = Sizes(1.0.inch)
        context.documentInfo.tex.macros["\\R"] = "\\mathbb{R}"
        context.documentInfo.tex.macros["\\Z"] = "\\mathbb{Z}"

        val postRenderer =
            HtmlPostRenderer(context) {
                TemplateProcessor
                    .fromResourceName("/postrendering/html-test-wrapper.html")
                    .content("<p><em>Hello, world!</em></p>")
            }

        assertEquals(
            javaClass
                .getResourceAsStream("/postrendering/html-test-result.html")!!
                .reader()
                .readText()
                .normalizeLineSeparators(),
            postRenderer.createTemplateProcessor().process(),
        )
    }

    @Test
    fun `resource generation`() {
        val context = MutableContext(QuarkdownFlavor)
        context.documentInfo.type = DocumentType.SLIDES
        context.documentInfo.theme = DocumentTheme(color = "darko", layout = "minimal")
        context.attributes.hasMath = true
        context.attributes.hasCode = false

        val postRenderer = HtmlPostRenderer(context)
        val html = "<html><head></head><body></body></html>"

        val resources = postRenderer.generateResources(html)
        assertEquals(3, resources.size)

        resources.filterIsInstance<TextOutputArtifact>().first { it.type == ArtifactType.HTML }.let {
            assertEquals(html, it.content)
        }

        val themeGroup = resources.filterIsInstance<OutputResourceGroup>().first { it.name == "theme" }

        themeGroup.resources.map { it.name }.let { themes ->
            assertTrue("darko" in themes)
            assertTrue("minimal" in themes)
            assertTrue("global" in themes)
            assertTrue("theme" in themes)
        }

        (themeGroup.resources.first { it.name == "theme" } as TextOutputArtifact).let {
            assertEquals(ArtifactType.CSS, it.type)
            assertEquals(
                """
                @import url('global.css');
                @import url('minimal.css');
                @import url('darko.css');
                """.trimIndent(),
                it.content,
            )
        }

        val scriptGroup = resources.filterIsInstance<OutputResourceGroup>().first { it.name == "script" }

        scriptGroup.resources.map { it.name }.let { scripts ->
            assertTrue("script" in scripts)
            assertTrue("slides" in scripts)
            assertTrue("math" in scripts)
            assertTrue("code" in scripts)
        }
    }

    @Test
    fun `resource generation, default theme`() {
        val context = MutableContext(QuarkdownFlavor)

        val postRenderer = HtmlPostRenderer(context)
        val html = "<html><head></head><body></body></html>"

        val resources = postRenderer.generateResources(html)
        assertEquals(3, resources.size)

        val themeGroup = resources.filterIsInstance<OutputResourceGroup>().first { it.name == "theme" }
        themeGroup.resources.map { it.name }.let { themes ->
            assertTrue("paperwhite" in themes) // Default
            assertTrue("latex" in themes) // Default
            assertTrue("global" in themes)
            assertTrue("theme" in themes)
        }

        (themeGroup.resources.first { it.name == "theme" } as TextOutputArtifact).let {
            assertEquals(ArtifactType.CSS, it.type)
            assertEquals(
                """
                @import url('global.css');
                @import url('latex.css');
                @import url('paperwhite.css');
                """.trimIndent(),
                it.content,
            )
        }
    }
}
