package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.SubdocumentRegistrationHook
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for subdocument registration from [SubdocumentLink].
 */
class SubdocumentRegistrationTest {
    private val context = MutableContext(QuarkdownFlavor)

    private val link1 =
        SubdocumentLink(
            Link(
                label = buildInline { text("Link") },
                url = "subdocument1.qd",
                title = null,
            ),
        )

    private fun traverse(root: Node) {
        context.subdocumentGraph = context.subdocumentGraph.addVertex(Subdocument.ROOT)
        ObservableAstIterator()
            .attach(SubdocumentRegistrationHook(context, failOnUnresolved = false))
            .traverse(root as NestableNode)
    }

    @Test
    fun single() {
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
    }
}
