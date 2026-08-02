package com.quarkdown.rendering.markdown

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
import com.quarkdown.core.ast.quarkdown.block.FileTree
import com.quarkdown.core.ast.quarkdown.block.FileTreeEntry
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.rendering.markdown.node.GfmNodeRenderer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [GfmNodeRenderer].
 */
class GfmNodeRendererTest {
    private fun Node.render(context: Context = MutableContext(QuarkdownFlavor)) = this.accept(GfmNodeRenderer(context))

    @Test
    fun `ast root, single child`() {
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
    fun `code block, with language`() {
        assertEquals(
            "```kotlin\nfun main() {\n    println(\"Hello, Quarkdown!\")\n}\n```\n\n",
            Code(
                language = "kotlin",
                content = "fun main() {\n    println(\"Hello, Quarkdown!\")\n}",
            ).render(),
        )
    }

    @Test
    fun `code block, without language`() {
        assertEquals(
            "```\nplain text\n```\n\n",
            Code(
                language = null,
                content = "plain text",
            ).render(),
        )
    }

    @Test
    fun `horizontal rule`() {
        assertEquals(
            "---\n\n",
            HorizontalRule.render(),
        )
    }

    @Test
    fun `heading h1`() {
        assertEquals(
            "# Hello\n\n",
            Heading(
                depth = 1,
                text = buildInline { text("Hello") },
            ).render(),
        )
    }

    @Test
    fun `heading h3`() {
        assertEquals(
            "### Hello\n\n",
            Heading(
                depth = 3,
                text = buildInline { text("Hello") },
            ).render(),
        )
    }

    @Test
    fun `heading with custom id`() {
        assertEquals(
            "<h2 id=\"greeting\">Hello</h2>\n\n",
            Heading(
                depth = 2,
                text = buildInline { text("Hello") },
                customId = "greeting",
            ).render(),
        )
    }

    @Test
    fun `link definition`() {
        assertEquals(
            "[example]: https://example.com \"Example\"\n\n",
            LinkDefinition(
                label = buildInline { text("example") },
                url = "https://example.com",
                title = listOf(Text("Example")),
            ).render(),
        )
    }

    @Test
    fun `link definition, no title`() {
        assertEquals(
            "[example]: https://example.com\n\n",
            LinkDefinition(
                label = buildInline { text("example") },
                url = "https://example.com",
                title = null,
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
            "- Item 1\n\n  - Subitem 1a\n  - Subitem 1b\n- Item 2\n\n",
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
                content =
                    buildBlocks {
                        paragraph { text("Hello") }
                    },
            ).render(),
        )
    }

    @Test
    fun `block quote multiline`() {
        assertEquals(
            "> Hello\n> \n> World\n\n",
            BlockQuote(
                content =
                    buildBlocks {
                        paragraph { text("Hello") }
                        paragraph { text("World") }
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
            "  \n",
            LineBreak.render(),
        )
    }

    @Test
    fun link() {
        assertEquals(
            "[Click here](https://example.com)",
            Link(
                url = "https://example.com",
                title = null,
                label = buildInline { text("Click here") },
            ).render(),
        )
    }

    @Test
    fun `link with title`() {
        assertEquals(
            "[Click here](https://example.com \"Example\")",
            Link(
                url = "https://example.com",
                title = buildInline { text("Example") },
                label = buildInline { text("Click here") },
            ).render(),
        )
    }

    @Test
    fun image() {
        assertEquals(
            "![Alt text](https://example.com/image.png)",
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
            "`println()`",
            CodeSpan("println()").render(),
        )
    }

    @Test
    fun emphasis() {
        assertEquals(
            "*Hello*",
            Emphasis(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun strong() {
        assertEquals(
            "**Hello**",
            Strong(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun `strong emphasis`() {
        assertEquals(
            "***Hello***",
            StrongEmphasis(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun strikethrough() {
        assertEquals(
            "~~Hello~~",
            Strikethrough(buildInline { text("Hello") }).render(),
        )
    }

    @Test
    fun math() {
        assertEquals(
            $$$"$$x^2 + y^2 = z^2$$\n\n",
            Math("x^2 + y^2 = z^2").render(),
        )
    }

    @Test
    fun `math span`() {
        assertEquals(
            $$"$x^2$",
            MathSpan("x^2").render(),
        )
    }

    @Test
    fun `mermaid diagram`() {
        assertEquals(
            "```mermaid\ngraph TD; A-->B;\n```\n\n",
            MermaidDiagram("graph TD; A-->B;").render(),
        )
    }

    @Test
    fun `box without title`() {
        assertEquals(
            "> Hello\n\n",
            Box(
                title = null,
                type = Box.Type.CALLOUT,
                content = buildBlocks { paragraph { text("Hello") } },
            ).render(),
        )
    }

    @Test
    fun `box with title`() {
        assertEquals(
            "> **Note**\n> \n> Hello\n\n",
            Box(
                title = buildInline { text("Note") },
                type = Box.Type.CALLOUT,
                content = buildBlocks { paragraph { text("Hello") } },
            ).render(),
        )
    }

    @Test
    fun `file tree, files only`() {
        assertEquals(
            "- file1.txt\n- file2.json\n\n",
            FileTree(
                listOf(
                    FileTreeEntry.File("file1.txt"),
                    FileTreeEntry.File("file2.json"),
                ),
            ).render(),
        )
    }

    @Test
    fun `file tree, directory with files`() {
        assertEquals(
            "- src/\n\n  - main.ts\n  - utils.ts\n- README.md\n\n",
            FileTree(
                listOf(
                    FileTreeEntry.Directory(
                        "src",
                        listOf(
                            FileTreeEntry.File("main.ts"),
                            FileTreeEntry.File("utils.ts"),
                        ),
                    ),
                    FileTreeEntry.File("README.md"),
                ),
            ).render(),
        )
    }

    @Test
    fun `keybinding, regular key`() {
        assertEquals(
            "<kbd>A</kbd>",
            Keybinding(listOf(Keybinding.Key("A"))).render(),
        )
    }

    @Test
    fun `keybinding, with modifiers`() {
        assertEquals(
            "<kbd>Ctrl/⌘</kbd>+<kbd>Shift</kbd>+<kbd>K</kbd>",
            Keybinding(
                listOf(Keybinding.PrimaryModifier, Keybinding.ShiftModifier, Keybinding.Key("K")),
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
            "# Table of Contents\n\n" +
                "1. [Welcome](#welcome)\n2. [Introduction](#introduction)\n\n   1. [Getting Started](#getting-started)\n\n",
            AstRoot(
                listOf(
                    Heading(depth = 1, text = buildInline { text("Table of Contents") }),
                    TableOfContentsView(maxDepth = 3),
                ),
            ).render(context),
        )
    }

    @Test
    fun table() {
        assertEquals(
            "| A | B |\n| --- | --- |\n| 1 | 2 |\n| 3 | 4 |\n\n",
            buildBlock {
                table {
                    column({ text("A") }) {
                        cell { text("1") }
                        cell { text("3") }
                    }
                    column({ text("B") }) {
                        cell { text("2") }
                        cell { text("4") }
                    }
                }
            }.render(),
        )
    }

    @Test
    fun `table cell with pipe is escaped`() {
        assertEquals(
            "| A\\|B |\n| --- |\n| x\\|y |\n\n",
            buildBlock {
                table {
                    column({ text("A|B") }) {
                        cell { text("x|y") }
                    }
                }
            }.render(),
        )
    }

    @Test
    fun `text escapes markdown metacharacters`() {
        assertEquals("2 \\* 3", Text("2 * 3").render())
        assertEquals("a\\_b", Text("a_b").render())
        assertEquals("\\[note", Text("[note").render())
        assertEquals("\\<div>", Text("<div>").render())
        assertEquals("path\\\\to", Text("path\\to").render())
        assertEquals("\\\$5", Text("\$5").render())
    }

    @Test
    fun `link title with embedded quote is escaped`() {
        assertEquals(
            "[Click](https://example.com \"He said \\\"hi\\\"\")",
            Link(
                url = "https://example.com",
                title = buildInline { text("He said \"hi\"") },
                label = buildInline { text("Click") },
            ).render(),
        )
    }

    @Test
    fun `code block content with triple backticks uses longer fence`() {
        assertEquals(
            "````\ntext with ``` inside\n````\n\n",
            Code(
                language = null,
                content = "text with ``` inside",
            ).render(),
        )
    }

    @Test
    fun `list item preserves interior blank line between paragraphs`() {
        assertEquals(
            "- First\n\n  Second\n\n",
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { text("First") }
                        paragraph { text("Second") }
                    }
                }
            }.first().render(),
        )
    }

    @Test
    fun `list item preserves nested fenced code block`() {
        assertEquals(
            "- First\n\n  ```\n  hello\n\n  world\n  ```\n\n",
            buildBlocks {
                unorderedList(loose = false) {
                    listItem {
                        paragraph { text("First") }
                        +Code(language = null, content = "hello\n\nworld")
                    }
                }
            }.first().render(),
        )
    }
}
