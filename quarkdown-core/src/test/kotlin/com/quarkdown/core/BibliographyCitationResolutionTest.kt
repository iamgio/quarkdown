package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryLabelProviderStrategy
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.reference.BibliographyCitationResolverHook
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CITATION_KEY = "einstein"

/**
 * Stub [BibliographyStyle] for tests that only need citation resolution, not formatting.
 */
private val stubStyle =
    object : BibliographyStyle {
        override val name = "stub"

        override val labelProvider =
            object : BibliographyEntryLabelProviderStrategy {
                override fun getCitationLabel(
                    entry: BibliographyEntry,
                    index: Int,
                ) = "[${index + 1}]"
            }

        override fun contentOf(entry: BibliographyEntry) = emptyList<Node>()
    }

/**
 * Tests for resolving bibliography citations to their bibliography entries.
 */
class BibliographyCitationResolutionTest {
    private val context = MutableContext(QuarkdownFlavor)

    private val bibliographyView =
        BibliographyView(
            bibliography =
                Bibliography(
                    listOf("einstein", "latexcompanion", "knuthwebsite")
                        .associateWith { BibliographyEntry(it) },
                ),
            style = stubStyle,
        )

    private val citation = BibliographyCitation(CITATION_KEY)

    private fun traverse(root: Node) {
        ObservableAstIterator()
            .attach(BibliographyCitationResolverHook(context))
            .traverse(root as NestableNode)
    }

    @Test
    fun `citation after bibliography`() {
        val root =
            buildBlock {
                root {
                    +bibliographyView
                    +citation
                }
            }

        traverse(root)

        assertEquals(
            bibliographyView.bibliography.entries[CITATION_KEY],
            citation.getDefinition(context)?.first,
        )
    }

    @Test
    fun `citation before bibliography`() {
        val root =
            buildBlock {
                root {
                    +citation
                    +bibliographyView
                }
            }

        traverse(root)

        assertEquals(
            bibliographyView.bibliography.entries[CITATION_KEY],
            citation.getDefinition(context)?.first,
        )
    }
}
