package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.document.locale.JVMLocaleLoader
import eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
import eu.iamgio.quarkdown.rendering.wrapper.TemplatePlaceholders
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * HTML post renderer tests.
 */
class HtmlPostRendererTest {
    @Test
    fun `render wrapper`() {
        assertEquals(
            "<html><head></head><body></body></html>",
            RenderWrapper("<html><head></head><body></body></html>").wrap(),
        )

        RenderWrapper("<body>[[CONTENT]]</body>")
            .value(TemplatePlaceholders.CONTENT, "<strong>Hello, world!</strong>")
            .let {
                assertEquals("<body><strong>Hello, world!</strong></body>", it.wrap())
            }

        RenderWrapper(
            """
            <body>
                [[CONTENT]]
            </body>
            """.trimIndent(),
        )
            .content("<strong>Hello, world!</strong>") // Shorthand
            .let {
                assertEquals("<body>\n    <strong>Hello, world!</strong>\n</body>", it.wrap())
            }

        RenderWrapper("<head><title>[[TITLE]]</title></head><body>[[CONTENT]]</body>")
            .value(TemplatePlaceholders.TITLE, "Doc title")
            .content("<strong>Hello, world!</strong>")
            .let {
                assertEquals(
                    "<head><title>Doc title</title></head><body><strong>Hello, world!</strong></body>",
                    it.wrap(),
                )
            }

        RenderWrapper("<body>[[if:CONDITION]][[CONTENT]][[endif:CONDITION]]</body>")
            .conditional("CONDITION", true)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body><em>Hello, world!</em></body>", it.wrap())
            }

        RenderWrapper("<body>[[if:CONDITION]][[CONTENT]][[endif:CONDITION]]</body>")
            .conditional("CONDITION", false)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body></body>", it.wrap())
            }

        RenderWrapper("<body>[[if:!CONDITION]][[CONTENT]][[endif:!CONDITION]]</body>")
            .conditional("CONDITION", true)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body></body>", it.wrap())
            }

        RenderWrapper(
            """
            <body>
                [[if:!CONDITION]]
                [[CONTENT]]
                [[endif:!CONDITION]]
            </body>
            """
                .trimIndent(),
        )
            .conditional("CONDITION", false)
            .content("<em>Hello, world!</em>")
            .let {
                assertEquals("<body>\n    <em>Hello, world!</em>\n</body>", it.wrap())
            }

        RenderWrapper(
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
        )
            .conditional("CONDITION", false)
            .content("Hello, world!")
            .let {
                assertEquals(
                    """
                    <body>
                        
                        <strong>Hello, world!</strong>
                    </body>
                    """.trimIndent(),
                    it.wrap(),
                )
            }

        RenderWrapper("<body>[[if:XYZ]]XYZ[[endif:XYZ]]</body>")
            .optionalValue("XYZ", "Hello, world!")
            .let {
                assertEquals("<body>XYZ</body>", it.wrap())
            }

        RenderWrapper("<body>[[if:XYZ]]XYZ[[endif:XYZ]]</body>")
            .optionalValue("XYZ", null)
            .let {
                assertEquals("<body></body>", it.wrap())
            }

        RenderWrapper(
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
        )
            .conditional(TemplatePlaceholders.IS_SLIDES, true)
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
                    it.wrap(),
                )
            }

        RenderWrapper.fromResourceName("/postrendering/html-test-wrapper.html")
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
                    javaClass.getResourceAsStream("/postrendering/html-test-result.html")!!
                        .reader()
                        .readText(),
                    it.wrap().replace("^ {4}\\R".toRegex(RegexOption.MULTILINE), ""),
                )
            }
    }
}
