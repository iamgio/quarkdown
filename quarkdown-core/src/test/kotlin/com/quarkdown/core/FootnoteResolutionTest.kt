package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.base.block.FootnoteDefinition
import com.quarkdown.core.ast.base.block.getIndex
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.reference.FootnoteResolverHook
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for resolving footnote references to their definitions.
 */
class FootnoteResolutionTest {
    private val context = MutableContext(QuarkdownFlavor)

    private val definition =
        FootnoteDefinition(
            label = "footnote1",
            text = buildInline { text("This is a footnote definition.") },
        )

    private val reference =
        ReferenceFootnote(
            label = "footnote1",
            fallback = { throw UnsupportedOperationException() },
        )

    private val invalidReference =
        ReferenceFootnote(
            label = "invalid",
            fallback = { throw UnsupportedOperationException() },
        )

    private fun traverse(root: Node) {
        ObservableAstIterator()
            .attach(FootnoteResolverHook(context))
            .traverse(root as NestableNode)
    }

    @Test
    fun `reference after definition`() {
        val root =
            buildBlock {
                root {
                    +definition
                    +reference
                }
            }

        traverse(root)

        assertEquals(
            definition,
            reference.getDefinition(context),
        )
        assertEquals(0, definition.getIndex(context))
    }

    @Test
    fun `reference before definition`() {
        val root =
            buildBlock {
                root {
                    +reference
                    +definition
                }
            }

        traverse(root)

        assertEquals(
            definition,
            reference.getDefinition(context),
        )
        assertEquals(0, definition.getIndex(context))
    }

    @Test
    fun `invalid reference`() {
        val root =
            buildBlock {
                root {
                    +invalidReference
                    +definition
                }
            }

        traverse(root)

        assertNull(reference.getDefinition(context))
        assertNull(definition.getIndex(context))
    }

    @Test
    fun `definitions in different order`() {
        val reference2 =
            ReferenceFootnote(
                label = "footnote2",
                fallback = { throw UnsupportedOperationException() },
            )

        val definition2 =
            FootnoteDefinition(
                label = "footnote2",
                text = buildInline { text("This is another footnote definition.") },
            )

        val root =
            buildBlock {
                root {
                    +reference2
                    +reference
                    +definition
                    +definition2
                }
            }

        traverse(root)

        assertEquals(0, definition2.getIndex(context))
        assertEquals(1, definition.getIndex(context))
    }
}
