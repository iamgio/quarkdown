package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.block.ImageFigure
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.location.LocationAwareLabelStorerHook
import com.quarkdown.core.context.hooks.location.LocationAwarenessHook
import com.quarkdown.core.context.hooks.reference.CrossReferenceResolverHook
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val ID = "ref-id"

/**
 * Tests for resolving cross-references to their definitions.
 */
class CrossReferenceResolutionTest {
    private val context = MutableContext(QuarkdownFlavor)

    private fun traverse(root: Node) {
        ObservableAstIterator()
            .attach(LocationAwarenessHook(context))
            .attach(LocationAwareLabelStorerHook(context))
            .attach(CrossReferenceResolverHook(context))
            .traverse(root as NestableNode)
    }

    @Test
    fun `reference after definition (heading)`() {
        val definition = Heading(depth = 2, text = buildInline { text("Heading") }, customId = ID)
        val reference = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +definition
                    +reference
                }
            }

        traverse(root)
        assertEquals(definition, reference.getDefinition(context))
    }

    @Test
    fun `reference before definition (heading)`() {
        val definition = Heading(depth = 6, text = buildInline { text("Heading") }, customId = ID)
        val reference = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +reference
                    +definition
                }
            }

        traverse(root)
        assertEquals(definition, reference.getDefinition(context))
    }

    @Test
    fun `invalid reference`() {
        val reference = CrossReference(ID)
        val root = buildBlock { root { +reference } }

        traverse(root)
        assertNull(reference.getDefinition(context))
    }

    @Test
    fun `multiple references to the same definition`() {
        val definition = Heading(depth = 3, text = buildInline { text("Heading") }, customId = ID)
        val reference1 = CrossReference(ID)
        val reference2 = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +reference1
                    +definition
                    +reference2
                }
            }

        traverse(root)
        assertEquals(definition, reference1.getDefinition(context))
        assertEquals(definition, reference2.getDefinition(context))
    }

    @Test
    fun `multiple definitions with the same ID - first one wins`() {
        val definition1 = Heading(depth = 4, text = buildInline { text("Heading 1") }, customId = ID)
        val definition2 = Heading(depth = 5, text = buildInline { text("Heading 2") }, customId = ID)
        val reference = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +definition1
                    +reference
                    +definition2
                }
            }

        traverse(root)
        assertEquals(definition1, reference.getDefinition(context))
    }

    @Test
    fun `mutual references (headings)`() {
        val definition1 = Heading(depth = 2, text = buildInline { text("Heading 1") }, customId = "id-1")
        val definition2 = Heading(depth = 3, text = buildInline { text("Heading 2") }, customId = "id-2")
        val reference1 = CrossReference("id-2")
        val reference2 = CrossReference("id-1")
        val root =
            buildBlock {
                root {
                    +definition1
                    +reference1
                    +definition2
                    +reference2
                }
            }

        traverse(root)
        assertEquals(definition2, reference1.getDefinition(context))
        assertEquals(definition1, reference2.getDefinition(context))
    }

    @Test
    fun `reference before definition (figures)`() {
        val definition =
            buildBlock {
                figure {
                    image("image.png", referenceId = ID) { text("An image") }
                }
            } as ImageFigure
        val reference = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +reference
                    +definition
                }
            }

        traverse(root)
        assertEquals(definition, reference.getDefinition(context))
    }

    @Test
    fun `reference after definition (tables)`() {
        val definition =
            buildBlock {
                table(referenceId = ID) {
                    column(header = { text("Header") }) {
                        cell { text("Cell 1") }
                        cell { text("Cell 2") }
                    }
                }
            } as Table
        val reference = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +definition
                    +reference
                }
            }

        traverse(root)
        assertEquals(definition, reference.getDefinition(context))
    }

    @Test
    fun `reference before definition (code blocks)`() {
        val definition =
            Code(
                content = "println(\"Hello, World!\")",
                language = "kotlin",
                referenceId = ID,
            )
        val reference = CrossReference(ID)
        val root =
            buildBlock {
                root {
                    +reference
                    +definition
                }
            }

        traverse(root)
        assertEquals(definition, reference.getDefinition(context))
    }
}
