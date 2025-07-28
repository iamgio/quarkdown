package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.getSubdocument
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.SubdocumentRegistrationHook
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals

private const val RESOURCE_PATH = "src/test/resources/subdoc"

/**
 * Tests for subdocument registration from [SubdocumentLink].
 */
class SubdocumentRegistrationTest {
    private val context = MutableContext(QuarkdownFlavor)

    private val link1 =
        SubdocumentLink(
            Link(
                label = buildInline { text("Link") },
                url = "$RESOURCE_PATH/subdoc-1.qd",
                title = null,
            ),
        )

    private val link2 =
        SubdocumentLink(
            Link(
                label = buildInline { text("Link") },
                url = "$RESOURCE_PATH/subdoc-2.qd",
                title = null,
            ),
        )

    private fun traverse(root: Node) {
        context.subdocumentGraph = context.subdocumentGraph.addVertex(Subdocument.ROOT)
        ObservableAstIterator()
            .attach(SubdocumentRegistrationHook(context))
            .traverse(root as NestableNode)
    }

    @Test
    fun `root to 1`() {
        val root =
            buildBlock {
                root {
                    +link1
                }
            }

        traverse(root)

        assertEquals(
            2,
            context.subdocumentGraph.vertices.size,
        )
        assertEquals(
            link1.getSubdocument(context),
            context.subdocumentGraph.getNeighbors(Subdocument.ROOT).single(),
        )
    }

    @Test
    fun `root to 1 and 2`() {
        val root =
            buildBlock {
                root {
                    +link1
                    +link2
                }
            }

        traverse(root)

        assertEquals(
            3,
            context.subdocumentGraph.vertices.size,
        )
        assertEquals(
            2,
            context.subdocumentGraph.getNeighbors(Subdocument.ROOT).count(),
        )
    }

    @Test
    fun `root to 1 twice`() {
        val root =
            buildBlock {
                root {
                    +link1
                    +link1
                }
            }

        traverse(root)

        assertEquals(
            2,
            context.subdocumentGraph.vertices.size,
        )
        assertEquals(
            1,
            context.subdocumentGraph.getNeighbors(Subdocument.ROOT).count(),
        )
    }
}
