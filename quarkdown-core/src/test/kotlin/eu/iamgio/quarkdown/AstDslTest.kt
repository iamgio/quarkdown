package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.Heading
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.block.Table
import eu.iamgio.quarkdown.ast.base.block.list.ListItem
import eu.iamgio.quarkdown.ast.base.block.list.OrderedList
import eu.iamgio.quarkdown.ast.base.block.list.TaskListItemVariant
import eu.iamgio.quarkdown.ast.base.block.list.UnorderedList
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
                        listItem(TaskListItemVariant(isChecked = true)) {
                            paragraph {
                                text("Item 2")
                            }
                        }
                    }
                    heading(3) {
                        text("Heading")
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
                                ListItem(children = listOf(Paragraph(listOf(Text("Item 1"))))),
                                ListItem(children = listOf(Paragraph(listOf(Text("Item 2"))))),
                            ),
                    ),
                    UnorderedList(
                        isLoose = false,
                        children =
                            listOf(
                                ListItem(children = listOf(Paragraph(listOf(Text("Item 1"))))),
                                ListItem(
                                    listOf(TaskListItemVariant(isChecked = true)),
                                    children = listOf(Paragraph(listOf(Text("Item 2")))),
                                ),
                            ),
                    ),
                    Heading(3, listOf(Text("Heading"))),
                ),
            ),
            root,
        )
    }

    @Test
    fun table() {
        val table =
            buildBlock {
                table {
                    column({ text("Key") }) {
                        cell { text("key1") }
                        cell { emphasis { text("key2") } }
                    }
                    column({ text("Value") }, alignment = Table.Alignment.CENTER) {
                        cell { text("true") }
                        cell { codeSpan("false") }
                    }
                }
            }

        assertNodeEquals(
            Table(
                columns =
                    listOf(
                        Table.Column(
                            alignment = Table.Alignment.NONE,
                            header = Table.Cell(listOf(Text("Key"))),
                            cells =
                                listOf(
                                    Table.Cell(listOf(Text("key1"))),
                                    Table.Cell(listOf(Emphasis(listOf(Text("key2"))))),
                                ),
                        ),
                        Table.Column(
                            alignment = Table.Alignment.CENTER,
                            header = Table.Cell(listOf(Text("Value"))),
                            cells =
                                listOf(
                                    Table.Cell(listOf(Text("true"))),
                                    Table.Cell(listOf(CodeSpan("false"))),
                                ),
                        ),
                    ),
            ),
            table,
        )
    }
}
