package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Paragraph
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.util.node.flattenedChildren
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tree traversal tests.
 */
class TreeTraversalTest {
    @Test
    fun `tree visit`() {
        val node =
            AstRoot(
                listOf(
                    BlockQuote(
                        content =
                            listOf(
                                Paragraph(listOf(Text("abc"))),
                            ),
                    ),
                    Paragraph(
                        listOf(
                            Strong(listOf(Text("abc"))),
                            Text("def"),
                            Emphasis(listOf(Text("ghi"))),
                        ),
                    ),
                    Code("Hello, world!", language = "java"),
                ),
            )

        with(node.flattenedChildren().map { it::class.simpleName }.toList()) {
            assertEquals(
                listOf(
                    "BlockQuote",
                    "Paragraph",
                    "Text",
                    "AstRoot", // Empty attribution group
                    "Paragraph",
                    "Strong",
                    "Text",
                    "Text",
                    "Emphasis",
                    "Text",
                    "Code",
                    "AstRoot", // Empty caption group
                ),
                this,
            )
        }

        // Iterator

        val blockQuoteHook =
            object : AstIteratorHook {
                override fun attach(iterator: ObservableAstIterator) {
                    iterator.on<BlockQuote> {
                        assertIs<Paragraph>(it.content.first())
                    }
                }
            }

        var finished = false

        ObservableAstIterator()
            .on<Strong> { assertNodeEquals(Text("abc"), it.children.first()) }
            .on<Emphasis> { assertNodeEquals(Text("ghi"), it.children.first()) }
            .attach(blockQuoteHook)
            .on<Code> {
                assertEquals("Hello, world!", it.content)
                assertEquals("java", it.language)
            }.onFinished { finished = true }
            .traverse(node)

        assertTrue(finished)
    }

    @Test
    fun `caption content is reachable via tree traversal`() {
        val captionContent = listOf(Text("My caption"), Strong(listOf(Text("bold"))))

        // Figure with caption.
        val figure =
            Figure(
                child = Paragraph(listOf(Text("figure content"))),
                caption = captionContent,
            )

        with(figure.flattenedChildren().map { it::class.simpleName }.toList()) {
            assertEquals(
                listOf(
                    "Paragraph",
                    "Text",
                    "AstRoot", // Caption group
                    "Text",
                    "Strong",
                    "Text",
                ),
                this,
            )
        }

        // Table with caption.
        val table =
            Table(
                columns =
                    listOf(
                        Table.Column(
                            alignment = Table.Alignment.NONE,
                            header = Table.Cell(listOf(Text("Header"))),
                            cells = listOf(Table.Cell(listOf(Text("Cell")))),
                        ),
                    ),
                caption = captionContent,
            )

        with(table.flattenedChildren().map { it::class.simpleName }.toList()) {
            assertEquals(
                listOf(
                    "Text", // Cell
                    "Text", // Header
                    "AstRoot", // Caption group
                    "Text",
                    "Strong",
                    "Text",
                ),
                this,
            )
        }

        // Code with caption.
        val code = Code("println()", language = "kotlin", caption = captionContent)

        with(code.flattenedChildren().map { it::class.simpleName }.toList()) {
            assertEquals(
                listOf(
                    "AstRoot", // Caption group
                    "Text",
                    "Strong",
                    "Text",
                ),
                this,
            )
        }
    }
}
