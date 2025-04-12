package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.ast.AstRoot
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.BlockQuote
import eu.iamgio.quarkdown.ast.base.block.Code
import eu.iamgio.quarkdown.ast.base.block.Paragraph
import eu.iamgio.quarkdown.ast.base.inline.Emphasis
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.dsl.buildBlock
import eu.iamgio.quarkdown.ast.dsl.buildBlocks
import eu.iamgio.quarkdown.ast.iterator.AstIteratorHook
import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.ast.quarkdown.block.Numbered
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.hooks.LocationAwareLabelStorerHook
import eu.iamgio.quarkdown.context.hooks.LocationAwarenessHook
import eu.iamgio.quarkdown.document.numbering.DocumentNumbering
import eu.iamgio.quarkdown.document.numbering.NumberingFormat
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.util.flattenedChildren
import eu.iamgio.quarkdown.util.toPlainText
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
                        children =
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
                    "Paragraph",
                    "Strong",
                    "Text",
                    "Text",
                    "Emphasis",
                    "Text",
                    "Code",
                ),
                this,
            )
        }

        // Iterator

        val blockQuoteHook =
            object : AstIteratorHook {
                override fun attach(iterator: ObservableAstIterator) {
                    iterator.on<BlockQuote> {
                        assertIs<Paragraph>(it.children.first())
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
            }
            .onFinished { finished = true }
            .traverse(node)

        assertTrue(finished)
    }

    @Test
    fun `heading numbering`() {
        val tree =
            buildBlock {
                root {
                    heading(1) { text("1") }
                    paragraph { text("...") }
                    heading(2) { text("1.A") }
                    paragraph { text("...") }
                    heading(2) { text("1.B") }
                    heading(3) { text("1.B.a") }
                    heading(2) { text("1.C") }
                    heading(4) { text("1.C.0.a") }
                    blockQuote {
                        heading(3) { text("1.C.a") }
                    }
                    heading(1) { text("2") }
                    heading(1) { text("3") }
                }
            } as AstRoot

        val context = MutableContext(QuarkdownFlavor)

        ObservableAstIterator()
            .attach(LocationAwarenessHook(context))
            .traverse(tree)

        val locations = context.attributes.locations

        assertEquals(
            mapOf(
                "1" to listOf(1),
                "1.A" to listOf(1, 1),
                "1.B" to listOf(1, 2),
                "1.B.a" to listOf(1, 2, 1),
                "1.C" to listOf(1, 3),
                "1.C.0.a" to listOf(1, 3, 0, 1),
                "1.C.a" to listOf(1, 3, 1),
                "2" to listOf(2),
                "3" to listOf(3),
            ),
            locations
                .mapKeys { (node, _) -> (node as TextNode).text.toPlainText() }
                .mapValues { (_, location) -> location.levels },
        )
    }

    @Test
    fun `figure numbering`() {
        val tree =
            buildBlock {
                root {
                    figure { image("img.png", title = "Caption") }
                    heading(1) { text("1") }
                    figure { image("img.png", title = "Caption") }
                    heading(2) { text("1.A") }
                    figure { image("img.png", title = "Caption") }
                    figure { image("img.png", title = "Caption") }
                    heading(2) { text("1.B") }
                    figure { image("img.png", title = "Caption") }
                    heading(1) { text("2") }
                    figure { image("img.png", title = "Caption") }
                    figure { image("img.png", title = "Caption") }
                }
            } as AstRoot

        fun getLabels(format: String): List<String> {
            val context = MutableContext(QuarkdownFlavor)

            context.documentInfo.numbering =
                DocumentNumbering(
                    figures = NumberingFormat.fromString(format),
                )

            ObservableAstIterator()
                .attach(LocationAwarenessHook(context))
                .attach(LocationAwareLabelStorerHook(context))
                .traverse(tree)

            return context.attributes.positionalLabels.values.toList()
        }

        assertEquals(
            listOf("0.1", "1.1", "1.2", "1.3", "1.4", "2.1", "2.2"),
            getLabels("1.1"),
        )

        assertEquals(
            listOf("0.i", "1.i", "1.ii", "1.iii", "1.iv", "2.i", "2.ii"),
            getLabels("1.i"),
        )

        assertEquals(
            listOf("0.0.a", "1.0.a", "1.A.a", "1.A.b", "1.B.a", "2.0.a", "2.0.b"),
            getLabels("1.A.a"),
        )
    }

    @Test
    fun `custom numbering`() {
        fun numbered(key: String) =
            Numbered(key) { location ->
                buildBlocks { paragraph { text("Hi from $location.") } }
            }

        val tree =
            buildBlock {
                root {
                    +numbered("key1")
                    heading(1) { text("1") }
                    +numbered("key1")
                    +numbered("key2")
                    heading(2) { text("1.A") }
                    +numbered("key1")
                    heading(1) { text("2") }
                    +numbered("key2")
                    +numbered("key1")
                }
            } as AstRoot

        val context = MutableContext(QuarkdownFlavor)

        context.documentInfo.numbering =
            DocumentNumbering(
                extra =
                    mapOf(
                        "key1" to NumberingFormat.fromString("1.1"),
                        "key2" to NumberingFormat.fromString("A"),
                    ),
            )

        ObservableAstIterator()
            .attach(LocationAwarenessHook(context))
            .attach(LocationAwareLabelStorerHook(context))
            .traverse(tree)

        assertEquals(
            listOf("0.1", "1.1", "A", "1.2", "B", "2.1"),
            context.attributes.positionalLabels.values.toList(),
        )
    }
}
