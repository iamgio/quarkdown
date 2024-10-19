package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.base.block.BaseListItem
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.OrderedList
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.block.TaskListItem
import eu.iamgio.quarkdown.ast.base.block.UnorderedList
import eu.iamgio.quarkdown.ast.base.inline.CodeSpan
import eu.iamgio.quarkdown.ast.base.inline.Emphasis
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.LineBreak
import eu.iamgio.quarkdown.ast.base.inline.Link
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.dsl.buildBlock
import kotlin.test.Test

/**
 * Tests for the AST building DSL.
 * @see eu.iamgio.quarkdown.ast.dsl
 */
class AstDslTest {
    @Test
    fun dsl() {
        val root =
            buildBlock {
                root {
                    paragraph {
                        text("Hello, ")
                        lineBreak()
                        strong { codeSpan("world") }
                        text("!")
                    }
                    blockQuote {
                        paragraph {
                            emphasis {
                                text("Block")
                                strong { text("quote") }
                                image("url", "title") { strong { text("alt") } }
                            }
                        }
                        +Code(content = "println(\"Hello, world!\")", language = "kotlin")
                    }
                    orderedList(startIndex = 1, loose = true) {
                        listItem {
                            paragraph {
                                text("Item 1")
                            }
                        }
                        listItem {
                            paragraph {
                                text("Item 2")
                            }
                        }
                    }
                    unorderedList(loose = false) {
                        listItem {
                            paragraph {
                                text("Item 1")
                            }
                        }
                        taskListItem(checked = true) {
                            paragraph {
                                text("Item 2")
                            }
                        }
                    }
                }
            }

        assertNodeEquals(
            AstRoot(
                listOf(
                    Paragraph(
                        listOf(
                            Text("Hello, "),
                            LineBreak,
                            Strong(listOf(CodeSpan("world"))),
                            Text("!"),
                        ),
                    ),
                    BlockQuote(
                        children =
                            listOf(
                                Paragraph(
                                    listOf(
                                        Emphasis(
                                            listOf(
                                                Text("Block"),
                                                Strong(listOf(Text("quote"))),
                                                Image(
                                                    link =
                                                        Link(
                                                            listOf(Strong(listOf(Text("alt")))),
                                                            url = "url",
                                                            title = "title",
                                                        ),
                                                    width = null,
                                                    height = null,
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                                Code("println(\"Hello, world!\")", "kotlin"),
                            ),
                    ),
                    OrderedList(
                        startIndex = 1,
                        isLoose = true,
                        children =
                            listOf(
                                BaseListItem(children = listOf(Paragraph(listOf(Text("Item 1"))))),
                                BaseListItem(children = listOf(Paragraph(listOf(Text("Item 2"))))),
                            ),
                    ),
                    UnorderedList(
                        isLoose = false,
                        children =
                            listOf(
                                BaseListItem(children = listOf(Paragraph(listOf(Text("Item 1"))))),
                                TaskListItem(isChecked = true, children = listOf(Paragraph(listOf(Text("Item 2"))))),
                            ),
                    ),
                ),
            ),
            root,
        )
    }
}
