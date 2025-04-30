package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.block.list.ListItem
import com.quarkdown.core.ast.base.block.list.OrderedList
import com.quarkdown.core.ast.base.block.list.TaskListItemVariant
import com.quarkdown.core.ast.base.block.list.UnorderedList
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildBlock
import kotlin.test.Test

/**
 * Tests for the AST building DSL.
 * @see com.quarkdown.core.ast.dsl
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
