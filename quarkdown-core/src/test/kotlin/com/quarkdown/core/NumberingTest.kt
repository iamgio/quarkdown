package com.quarkdown.core

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.getLocation
import com.quarkdown.core.ast.attributes.location.getLocationLabel
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildBlocks
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.location.LocationAwareLabelStorerHook
import com.quarkdown.core.context.hooks.location.LocationAwarenessHook
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.util.flattenedChildren
import com.quarkdown.core.util.toPlainText
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for element numbering.
 */
class NumberingTest {
    private fun getLabels(
        tree: NestableNode,
        numbering: DocumentNumbering,
    ): List<String> {
        val context = MutableContext(QuarkdownFlavor)
        context.documentInfo = context.documentInfo.copy(numbering = numbering)

        ObservableAstIterator()
            .attach(LocationAwarenessHook(context))
            .attach(LocationAwareLabelStorerHook(context))
            .traverse(tree)

        return tree
            .flattenedChildren()
            .filterIsInstance<LocationTrackableNode>()
            .mapNotNull { it.getLocationLabel(context) }
            .toList()
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
            tree
                .flattenedChildren()
                .filterIsInstance<LocationTrackableNode>()
                .associateWith { it.getLocation(context)!! }
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

        val numbering = { format: String -> DocumentNumbering(figures = NumberingFormat.fromString(format)) }

        assertEquals(
            listOf("0.1", "1.1", "1.2", "1.3", "1.4", "2.1", "2.2"),
            getLabels(tree, numbering("1.1")),
        )

        assertEquals(
            listOf("0.i", "1.i", "1.ii", "1.iii", "1.iv", "2.i", "2.ii"),
            getLabels(tree, numbering("1.i")),
        )

        assertEquals(
            listOf("0.0.a", "1.0.a", "1.A.a", "1.A.b", "1.B.a", "2.0.a", "2.0.b"),
            getLabels(tree, numbering("1.A.a")),
        )
    }

    @Test
    fun `code block numbering`() {
        val tree =
            buildBlock {
                root {
                    +Code("Code block 1", language = "java")
                    heading(1) { text("1") }
                    +Code("Code block 2", language = "java")
                    heading(2) { text("1.A") }
                    +Code("Code block 3", language = "java")
                    +Code("Code block 4", language = "java")
                    heading(2) { text("1.B") }
                    +Code("Code block 5", language = "java")
                    heading(1) { text("2") }
                    +Code("Code block 6", language = "java")
                    +Code("Code block 7", language = "java")
                }
            } as AstRoot

        assertEquals(
            listOf("0.1", "1.1", "1.2", "1.3", "1.4", "2.1", "2.2"),
            getLabels(tree, DocumentNumbering(codeBlocks = NumberingFormat.fromString("1.1"))),
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

        val labels =
            getLabels(
                tree,
                DocumentNumbering(
                    extra =
                        mapOf(
                            "key1" to NumberingFormat.fromString("1.1"),
                            "key2" to NumberingFormat.fromString("A"),
                        ),
                ),
            )

        assertEquals(
            listOf("0.1", "1.1", "A", "1.2", "B", "2.1"),
            labels,
        )
    }
}
