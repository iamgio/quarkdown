package com.quarkdown.core.bibliography.citation

import com.quarkdown.core.ast.quarkdown.bibliography.ResolvedBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.property.Property

/**
 * [Property] that is assigned to each [com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation].
 * It contains the [BibliographyEntry] that the citation refers to, resolved from the document's bibliographies.
 * @see BibliographyEntry
 * @see com.quarkdown.core.context.hooks.bibliography.BibliographyCitationHook for the assignment stage
 */
data class ResolvedBibliographyEntryProperty(
    override val value: ResolvedBibliographyEntry,
) : Property<ResolvedBibliographyEntry> {
    companion object : Property.Key<ResolvedBibliographyEntry>

    override val key = ResolvedBibliographyEntryProperty
}
