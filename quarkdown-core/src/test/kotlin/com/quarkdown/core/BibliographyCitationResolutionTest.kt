package com.quarkdown.core

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.dsl.buildBlock
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.reference.BibliographyCitationHook
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import kotlin.test.Test
import kotlin.test.assertEquals

private const val CITATION_KEY = "einstein"

/**
 * Tests for resolving bibliography citations to their bibliography entries.
 */
class BibliographyCitationResolutionTest {
    private val context = MutableContext(QuarkdownFlavor)

    private val bibliographyView =
        BibliographyView(
            title = null,
            bibliography =
                Bibliography(
                    listOf(
                        BibliographySamples.article,
                        BibliographySamples.book,
                        BibliographySamples.misc,
                    ),
                ),
            style = BibliographyStyle.Plain,
        )

    private val citation = BibliographyCitation(CITATION_KEY)

    private fun traverse(root: Node) {
        ObservableAstIterator()
            .attach(BibliographyCitationHook(context))
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
