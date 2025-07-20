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

    private fun def(label: String) =
        FootnoteDefinition(
            label = label,
            text = buildInline { text("This is a footnote definition for $label.") },
        )

    private fun ref(label: String) =
        ReferenceFootnote(
            label = label,
            fallback = { throw UnsupportedOperationException("No fallback for $label") },
        )

    private val definition1 = def("footnote1")
    private val reference1 = ref("footnote1")

    private val definition2 = def("footnote2")
    private val reference2 = ref("footnote2")

    private val invalidReference = ref("invalidFootnote")

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
                    +definition1
                    +reference1
                }
            }

        traverse(root)

        assertEquals(
            definition1,
            reference1.getDefinition(context),
        )
        assertEquals(0, definition1.getIndex(context))
    }

    @Test
    fun `reference before definition`() {
        val root =
            buildBlock {
                root {
                    +reference1
                    +definition1
                }
            }

        traverse(root)

        assertEquals(
            definition1,
            reference1.getDefinition(context),
        )
        assertEquals(0, definition1.getIndex(context))
    }

    @Test
    fun `invalid reference`() {
        val root =
            buildBlock {
                root {
                    +invalidReference
                    +definition1
                }
            }

        traverse(root)

        assertNull(reference1.getDefinition(context))
        assertNull(definition1.getIndex(context))
    }

    @Test
    fun `definitions in different order`() {
        val root =
            buildBlock {
                root {
                    +reference2
                    +reference1
                    +definition1
                    +definition2
                }
            }

        traverse(root)

        assertEquals(0, definition2.getIndex(context))
        assertEquals(1, definition1.getIndex(context))
    }

    @Test
    fun `multiple references to the same definition`() {
        val root =
            buildBlock {
                root {
                    +definition1
                    +reference1
                    +reference1

                    +definition2
                    +reference2
                    +reference2
                    +reference2
                }
            }

        traverse(root)

        assertEquals(
            definition1,
            reference1.getDefinition(context),
        )
        assertEquals(0, definition1.getIndex(context))
        assertEquals(1, definition2.getIndex(context))
    }
}
