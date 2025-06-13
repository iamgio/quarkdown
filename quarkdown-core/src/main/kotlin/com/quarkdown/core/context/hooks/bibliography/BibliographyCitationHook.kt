package com.quarkdown.core.context.hooks.bibliography

import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.bibliography.ResolvedBibliographyEntry
import com.quarkdown.core.ast.quarkdown.bibliography.setEntry
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.context.MutableContext

/**
 * Hook that associates a bibliography entry to each [BibliographyCitation]
 * that can be linked to an entry of a [Bibliography]
 * within a [BibliographyView].
 */
class BibliographyCitationHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        val bibliographies = iterator.collectAll<BibliographyView>()
        val citations = iterator.collectAll<BibliographyCitation>()

        iterator.onFinished {
            citations.forEach { citation ->
                attachEntry(citation, bibliographies)
            }
        }
    }

    /**
     * Attaches the bibliography entry to the citation if it exists in any of the provided bibliographies.
     * If the entry is found, it is attached to the citation as a [com.quarkdown.core.bibliography.citation.ResolvedBibliographyEntryProperty].
     * @param citation the citation to attach the entry to
     * @param bibliographies the list of bibliographies to search for the entry
     */
    private fun attachEntry(
        citation: BibliographyCitation,
        bibliographies: List<BibliographyView>,
    ) {
        val entry: ResolvedBibliographyEntry? =
            bibliographies
                .firstNotNullOfOrNull { bibliography ->
                    bibliography.bibliography
                        .entries[citation.citationKey]
                        ?.let { it to bibliography }
                }

        entry?.let {
            citation.setEntry(context, it)
        }
    }
}
