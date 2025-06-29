package com.quarkdown.core.context.hooks.reference

import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.context.MutableContext

/**
 * Hook that associates a bibliography entry to each [BibliographyCitation]
 * that can be linked to an entry of a [Bibliography]
 * within a [BibliographyView].
 */
class BibliographyCitationResolverHook(
    context: MutableContext,
) : ReferenceDefinitionResolverHook<BibliographyCitation, BibliographyView, Pair<BibliographyEntry, BibliographyView>>(context) {
    override fun collectReferences(iterator: ObservableAstIterator) = iterator.collectAll<BibliographyCitation>()

    override fun collectDefinitions(iterator: ObservableAstIterator) = iterator.collectAll<BibliographyView>()

    override fun findDefinitionPair(
        reference: BibliographyCitation,
        definitions: List<BibliographyView>,
    ): Pair<BibliographyView, Pair<BibliographyEntry, BibliographyView>>? =
        definitions
            .firstNotNullOfOrNull { bibliography ->
                bibliography.bibliography
                    .entries[reference.citationKey]
                    ?.let { bibliography to (it to bibliography) }
            }

    override fun transformDefinitionPair(definition: Pair<BibliographyView, Pair<BibliographyEntry, BibliographyView>>) = definition.second
}
