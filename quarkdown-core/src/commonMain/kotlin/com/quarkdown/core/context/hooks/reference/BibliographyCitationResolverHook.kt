package com.quarkdown.core.context.hooks.reference

import com.quarkdown.core.ast.attributes.reference.ReferenceNode
import com.quarkdown.core.ast.attributes.reference.setCitationLabel
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.context.MutableContext

/**
 * Hook that associates bibliography entries to each [BibliographyCitation]
 * that can be linked to entries of a [Bibliography]
 * within a [BibliographyView].
 *
 * After each citation is resolved, this hook pre-computes its citation label in document order
 * and stores it as a [com.quarkdown.core.ast.attributes.reference.CitationLabelProperty] on the node,
 * so that parallel rendering can safely read it without depending on visit order.
 */
class BibliographyCitationResolverHook(
    context: MutableContext,
) : ReferenceDefinitionResolverHook<BibliographyCitation, BibliographyView, Pair<List<BibliographyEntry>, BibliographyView>>(context) {
    override fun collectReferences(iterator: ObservableAstIterator) = iterator.collectAll<BibliographyCitation>()

    override fun collectDefinitions(iterator: ObservableAstIterator) = iterator.collectAll<BibliographyView>()

    override fun findDefinitionPair(
        reference: BibliographyCitation,
        definitions: List<BibliographyView>,
        index: Int,
    ): Pair<BibliographyView, Pair<List<BibliographyEntry>, BibliographyView>>? =
        definitions
            .firstNotNullOfOrNull { bibliography ->
                val entries =
                    reference.citationKeys.map { key ->
                        bibliography.bibliography.entries[key] ?: return@firstNotNullOfOrNull null
                    }
                bibliography to (entries to bibliography)
            }

    override fun onResolved(
        reference: ReferenceNode<BibliographyCitation, Pair<List<BibliographyEntry>, BibliographyView>>,
        definition: Pair<List<BibliographyEntry>, BibliographyView>,
    ) {
        val (entries, view) = definition
        val label = view.style.labelProvider.getCitationLabel(entries)
        reference.reference.setCitationLabel(context, label)
    }
}
