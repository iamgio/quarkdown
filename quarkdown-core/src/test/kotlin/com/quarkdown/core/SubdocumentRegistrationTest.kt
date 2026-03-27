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
import com.quarkdown.core.context.hooks.UnresolvedSubdocumentException
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val RESOURCE_PATH = "src/test/resources/subdoc"

/**
 * Tests for subdocument registration from [SubdocumentLink].
 */
class SubdocumentRegistrationTest {
    private val context = MutableContext(QuarkdownFlavor)

    private fun link1() =
        SubdocumentLink(
            Link(
                label = buildInline { text("Link") },
                url = "$RESOURCE_PATH/subdoc-1.qd",
                title = null,
            ),
        )

    private fun link2() =
        SubdocumentLink(
            Link(
                label = buildInline { text("Link") },
                url = "$RESOURCE_PATH/subdoc-2.qd",
                title = null,
            ),
        )

    private fun invalidLink() =
        SubdocumentLink(
            Link(
                label = buildInline { text("Invalid Link") },
                url = "$RESOURCE_PATH/nonexistent.qd",
                title = null,
            ),
        )

    private fun traverse(root: Node) {
        context.sharedSubdocumentsData =
            context.sharedSubdocumentsData.copy(graph = context.sharedSubdocumentsData.graph.addVertex(Subdocument.Root))
        ObservableAstIterator()
            .attach(SubdocumentRegistrationHook(context))
            .traverse(root as NestableNode)
    }

    @Test
    fun `root to 1`() {
        val link = link1()
        val root =
            buildBlock {
                root {
                    +link
                }
            }

        traverse(root)

        assertEquals(
            2,
            context.sharedSubdocumentsData.graph.vertices.size,
        )
        assertEquals(
            link.getSubdocument(context),
            context.sharedSubdocumentsData.graph
                .getNeighbors(Subdocument.Root)
                .single(),
        )
        assertNull(link.error)
    }

    @Test
    fun `root to 1 and 2`() {
        val link1 = link1()
        val link2 = link2()
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
            context.sharedSubdocumentsData.graph.vertices.size,
        )
        assertEquals(
            2,
            context.sharedSubdocumentsData.graph
                .getNeighbors(Subdocument.Root)
                .count(),
        )
    }

    @Test
    fun `root to 1 twice`() {
        val link = link1()
        val root =
            buildBlock {
                root {
                    +link
                    +link
                }
            }

        traverse(root)

        assertEquals(
            2,
            context.sharedSubdocumentsData.graph.vertices.size,
        )
        assertEquals(
            1,
            context.sharedSubdocumentsData.graph
                .getNeighbors(Subdocument.Root)
                .count(),
        )
    }

    @Test
    fun `invalid link, no error handler`() {
        val link = invalidLink()
        val root =
            buildBlock {
                root {
                    +link
                }
            }

        assertFailsWith<UnresolvedSubdocumentException> {
            traverse(root)
        }
    }

    @Test
    fun `invalid link, with error handler`() {
        context.attachMockPipeline(
            options =
                PipelineOptions(
                    errorHandler = BasePipelineErrorHandler(),
                ),
        )

        val link = invalidLink()
        val root =
            buildBlock {
                root {
                    +link
                }
            }

        traverse(root)

        assertEquals(
            1,
            context.sharedSubdocumentsData.graph.vertices.size,
        )
        assertNotNull(link.error)
    }
}
