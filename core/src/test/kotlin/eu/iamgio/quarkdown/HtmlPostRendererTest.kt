package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.document.DocumentTheme
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.localization.jvm.JVMLocaleLoader
import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer
import eu.iamgio.quarkdown.rendering.template.TemplatePlaceholders
import eu.iamgio.quarkdown.template.TemplateProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * HTML post renderer tests.
 */
class HtmlPostRendererTest {
    @Test
    fun `template processor`() {
        assertEquals(
            "<html><head></head><body></body></html>",
            TemplateProcessor("<html><head></head><body></body></html>").build(),
        )

        TemplateProcessor("<body>[[CONTENT]]</body>")
            .value(TemplatePlaceholders.CONTENT, "<strong>Hello, world!</strong>")
            .let {
                assertEquals("<body><strong>Hello, world!</strong></body>", it.build())
            }

        TemplateProcessor(
            """
            <body>
                [[CONTENT]]
            </body>
            """.trimIndent(),
        ).content("<strong>Hello, world!</strong>") // Shorthand
            .let {
                assertEquals("<body>\n    <strong>Hello, world!</strong>\n</body>", it.build())
            }

        TemplateProcessor("<head><title>[[TITLE]]</title></head><body>[[CONTENT]]</body>")
            .value(TemplatePlaceholders.TITLE, "Doc title")
            .content("<strong>Hello, world!</strong>")
            .let {
                assertEquals(
                    "<head><title>Doc title</title></head><body><strong>Hello, world!</strong></body>",
                    it.build(),
                )
            }

        TemplateProcessor("<body>[[if:CONDITION]][[CONTENT]][[endif:CONDITION]]</body>")
            .conditional("CONDITION", true)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body><em>Hello, world!</em></body>", it.build())
            }

        TemplateProcessor("<body>[[if:CONDITION]][[CONTENT]][[endif:CONDITION]]</body>")
            .conditional("CONDITION", false)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body></body>", it.build())
            }

        TemplateProcessor("<body>[[if:!CONDITION]][[CONTENT]][[endif:!CONDITION]]</body>")
            .conditional("CONDITION", true)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body></body>", it.build())
            }

        TemplateProcessor(
            """
            <body>
                [[if:!CONDITION]]
                [[CONTENT]]
                [[endif:!CONDITION]]
            </body>
            """.trimIndent(),
        ).conditional("CONDITION", false)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body>\n    <em>Hello, world!</em>\n</body>", it.build())
            }

        TemplateProcessor(
            """
            <body>
                [[if:CONDITION]]
                <em>Hello, world!</em>
                [[endif:CONDITION]]
                [[if:!CONDITION]]
                <strong>Hello, world!</strong>
                [[endif:!CONDITION]]
            </body>
            """.trimIndent(),
        ).conditional("CONDITION", false)
            .content("Hello, world!")
            .let {
                assertEquals(
                    """
                    <body>
                        
                        <strong>Hello, world!</strong>
                    </body>
                    """.trimIndent(),
                    it.build(),
                )
            }

        TemplateProcessor("<body>[[if:XYZ]]XYZ[[endif:XYZ]]</body>")
            .optionalValue("XYZ", "Hello, world!")
            .let {
                assertEquals("<body>XYZ</body>", it.build())
            }

        TemplateProcessor("<body>[[if:XYZ]]XYZ[[endif:XYZ]]</body>")
            .optionalValue("XYZ", null)
            .let {
                assertEquals("<body></body>", it.build())
            }

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
        ).conditional(TemplatePlaceholders.IS_SLIDES, true)
            .optionalValue(TemplatePlaceholders.LANGUAGE, JVMLocaleLoader.fromName("english")?.tag)
            .value(TemplatePlaceholders.TITLE, "Quarkdown")
            .conditional(TemplatePlaceholders.HAS_CODE, false)
            .conditional(TemplatePlaceholders.HAS_MATH, true)
            .content("<p><em>Hello, world!</em></p>")
            .let {
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
                    it.build(),
                )
            }

        TemplateProcessor
            .fromResourceName("/postrendering/html-test-wrapper.html")
            .optionalValue(TemplatePlaceholders.LANGUAGE, JVMLocaleLoader.fromName("english")?.tag)
            .value(TemplatePlaceholders.TITLE, "Quarkdown")
            .conditional(TemplatePlaceholders.IS_PAGED, false)
            .conditional(TemplatePlaceholders.IS_SLIDES, true)
            .conditional(TemplatePlaceholders.HAS_CODE, true)
            .conditional(TemplatePlaceholders.HAS_MATH, false)
            .conditional(TemplatePlaceholders.HAS_PAGE_SIZE, true)
            .value(TemplatePlaceholders.PAGE_WIDTH, "8.5in")
            .value(TemplatePlaceholders.PAGE_HEIGHT, "11in")
            .optionalValue(TemplatePlaceholders.PAGE_MARGIN, "1in")
            .content("<p><em>Hello, world!</em></p>")
            .let {
                assertEquals(
                    javaClass
                        .getResourceAsStream("/postrendering/html-test-result.html")!!
                        .reader()
                        .readText(),
                    it.build().replace("^ {4}\\R".toRegex(RegexOption.MULTILINE), ""),
                )
            }
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
            assertTrue("code" !in scripts)
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
