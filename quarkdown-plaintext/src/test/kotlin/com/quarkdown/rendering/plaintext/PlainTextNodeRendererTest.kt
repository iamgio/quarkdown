package com.quarkdown.rendering.plaintext

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.HorizontalRule
import com.quarkdown.core.ast.base.block.Html
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.base.inline.CheckBox
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Comment
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildBlocks
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.rendering.plaintext.node.PlainTextNodeRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [PlainTextNodeRenderer].
 */
class PlainTextNodeRendererTest {
    private fun Node.render(context: Context = MutableContext(QuarkdownFlavor)) = this.accept(PlainTextNodeRenderer(context))

    @Test
    fun `ast root, single child`() {
        // Trailing newlines are trimmed later in post-rendering.
        assertEquals(
            "Hello, Quarkdown!\n\n",
            AstRoot(
                buildBlocks {
                    paragraph {
                        text("Hello, Quarkdown!")
                    }
                },
            ).render(),
        )
    }

    @Test
    fun `ast root, multiple children`() {
        assertEquals(
            "Hello, Quarkdown!\n\nLearn more on GitHub.\n\n",
            AstRoot(
                buildBlocks {
                    paragraph {
                        text("Hello, Quarkdown!")
                    }
                    paragraph {
                        text("Learn more on GitHub.")
                    }
                },
            ).render(),
        )
    }

    @Test
    fun `code block`() {
        assertEquals(
            "\tfun main() {\n\t    println(\"Hello, Quarkdown!\")\n\t}\n\n",
            Code(
                language = "kotlin",
                content = "fun main() {\n    println(\"Hello, Quarkdown!\")\n}",
            ).render(),
        )
    }

    @Test
    fun `horizontal rule`() {
        assertEquals(
            "-----\n\n",
            HorizontalRule.render(),
        )
    }

    @Test
    fun heading() {
        assertEquals(
            "Hello\n\n",
            Heading(
                depth = 1,
                text = buildInline { text("Hello") },
            ).render(),
        )
    }

    @Test
    fun `link definition`() {
        assertEquals(
            "",
            LinkDefinition(
                label = buildInline { text("example") },
                url = "https://example.com",
                title = "Example",
            ).render(),
        )
    }

    @Test
    fun `ordered list, tight`() {
        assertEquals(
            "1. First\n2. Second\n3. Third\n\n",
            buildBlocks {
                orderedList(loose = false) {
                    listItem { paragraph { text("First") } }
                    listItem { paragraph { text("Second") } }
                    listItem { paragraph { text("Third") } }
                }
            }.first().render(),
        )
    }

    @Test
    fun `ordered list, loose`() {
        assertEquals(
            "1. First\n\n2. Second\n\n3. Third\n\n",
            buildBlocks {
                orderedList(loose = true) {
                    listItem { paragraph { text("First") } }
                    listItem { paragraph { text("Second") } }
                    listItem { paragraph { text("Third") } }
                }
            }.first().render(),
        )
    }

    @Test
    fun `ordered list, nested`() {
        assertEquals(
            "1. Item 1\n2. Item 2\n\t1. Subitem 2a\n\t2. Subitem 2b\n3. Item 3\n\n",
            buildBlock {
                orderedList(loose = false) {
                    listItem { paragraph { text("Item 1") } }
                    listItem {
                        paragraph { text("Item 2") }
                        orderedList(loose = false) {
                            listItem { paragraph { text("Subitem 2a") } }
                            listItem { paragraph { text("Subitem 2b") } }
                        }
                    }
                    listItem { paragraph { text("Item 3") } }
                }
            }.render(),
        )
    }

    @Test
    fun `unordered list, tight`() {
        assertEquals(
            "- First\n- Second\n- Third\n\n",
            buildBlocks {
                unorderedList(loose = false) {
                    listItem { paragraph { text("First") } }
                    listItem { paragraph { text("Second") } }
                    listItem { paragraph { text("Third") } }
                }
            }.first().render(),
        )
    }

    @Test
    fun `unordered list, loose`() {
        assertEquals(
            "- First\n\n- Second\n\n- Third\n\n",
            buildBlocks {
                unorderedList(loose = true) {
                    listItem { paragraph { text("First") } }
                    listItem { paragraph { text("Second") } }
                    listItem { paragraph { text("Third") } }
                }
            }.first().render(),
        )
    }

    @Test
    fun `unordered list, nested`() {
        assertEquals(
            "- Item 1\n\t- Subitem 1a\n\t- Subitem 1b\n- Item 2\n\n",
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { text("Item 1") }
                        unorderedList(loose = false) {
                            listItem { paragraph { text("Subitem 1a") } }
                            listItem { paragraph { text("Subitem 1b") } }
                        }
                    }
                    listItem { paragraph { text("Item 2") } }
                }
            }.first().render(),
        )
    }

    @Test
    fun html() {
        assertEquals(
            "",
            Html("<div>Hello</div>").render(),
        )
    }

    @Test
    fun `block quote`() {
        assertEquals(
            "> Hello\n\n",
            BlockQuote(
                children =
                    buildBlocks {
                        paragraph { text("Hello") }
                    },
            ).render(),
        )
    }

    @Test
    fun `block quote multiline`() {
        assertEquals(
            "> Hello\n> \n> \tWorld\n> \t!\n\n",
            BlockQuote(
                children =
                    buildBlocks {
                        paragraph { text("Hello") }

                        +Code(language = null, content = "World\n!")
                    },
            ).render(),
        )
    }

    @Test
    fun comment() {
        assertEquals(
            "",
            Comment.render(),
        )
    }

    @Test
    fun `line break`() {
        assertEquals(
            "\n",
            LineBreak.render(),
        )
    }

    @Test
    fun link() {
        assertEquals(
            "Click here",
            Link(
                url = "https://example.com",
                title = null,
                label = buildInline { text("Click here") },
            ).render(),
        )
    }

    @Test
    fun image() {
        assertEquals(
            "",
            Image(
                link =
                    Link(
                        url = "https://example.com/image.png",
                        title = null,
                        label = buildInline { text("Alt text") },
                    ),
                width = null,
                height = null,
            ).render(),
        )
    }

    @Test
    fun checkbox() {
        assertEquals("[x] ", CheckBox(isChecked = true).render())
        assertEquals("[ ] ", CheckBox(isChecked = false).render())
    }

    @Test
    fun text() {
        assertEquals(
            "Hello, Quarkdown!",
            Text("Hello, Quarkdown!").render(),
        )
    }

    @Test
    fun `code span`() {
        assertEquals(
            "println()",
            CodeSpan("println()").render(),
        )
    }

    @Test
    fun emphasis() {
        assertEquals(
            "Hello",
            Emphasis(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun strong() {
        assertEquals(
            "Hello",
            Strong(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun `strong emphasis`() {
        assertEquals(
            "Hello",
            StrongEmphasis(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun strikethrough() {
        assertEquals(
            "Hello",
            Strikethrough(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun math() {
        assertEquals(
            "x^2 + y^2 = z^2\n\n",
            Math("x^2 + y^2 = z^2").render(),
        )
    }

    @Test
    fun `math span`() {
        assertEquals(
            "x^2",
            MathSpan("x^2").render(),
        )
    }

    @Test
    fun `box without title`() {
        assertEquals(
            "Hello\n\n",
            Box(
                title = null,
                type = Box.Type.CALLOUT,
                children = buildBlocks { paragraph { text("Hello") } },
            ).render(),
        )
    }

    @Test
    fun `box with title`() {
        assertEquals(
            "Note\n-----\nHello\n\n",
            Box(
                title = buildInline { text("Note") },
                type = Box.Type.CALLOUT,
                children = buildBlocks { paragraph { text("Hello") } },
            ).render(),
        )
    }

    @Test
    fun `table of contents`() {
        val toc =
            TableOfContents(
                items =
                    listOf(
                        TableOfContents.Item(
                            heading =
                                Heading(
                                    depth = 1,
                                    text = buildInline { text("Welcome") },
                                ),
                            subItems = emptyList(),
                        ),
                        TableOfContents.Item(
                            heading =
                                Heading(
                                    depth = 2,
                                    text = buildInline { text("Introduction") },
                                ),
                            subItems =
                                listOf(
                                    TableOfContents.Item(
                                        heading =
                                            Heading(
                                                depth = 3,
                                                text = buildInline { text("Getting Started") },
                                            ),
                                        subItems = emptyList(),
                                    ),
                                ),
                        ),
                    ),
            )

        val context = MutableContext(QuarkdownFlavor)
        context.attributes.tableOfContents = toc

        assertEquals(
            "Table of Contents\n\n" +
                "1. Welcome\n2. Introduction\n\t1. Getting Started\n\n",
            TableOfContentsView(
                title = buildInline { text("Table of Contents") },
                maxDepth = 3,
            ).render(context),
        )
    }
}
