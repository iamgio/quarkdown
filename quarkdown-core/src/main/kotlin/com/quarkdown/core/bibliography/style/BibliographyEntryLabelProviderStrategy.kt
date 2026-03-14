package com.quarkdown.core.bibliography.style

import com.quarkdown.core.bibliography.BibliographyEntry

/**
 * Supplier of citation labels for bibliography entries.
 * Labels serve two purposes:
 * - **Citation labels** appear inline in the document text (e.g. `[1]` or `(Einstein, 1905)`).
 * - **List labels** appear next to each entry in the bibliography list (e.g. `[1]` or empty for APA).
 */
interface BibliographyEntryLabelProviderStrategy {
    /**
     * Returns the label for an in-text citation (e.g. `[1]` or `(Einstein, 1905)`).
     * @param entry the bibliography entry being cited
     * @param index the index of the entry in the bibliography list, starting from 0
     */
    fun getCitationLabel(
        entry: BibliographyEntry,
        index: Int,
    ): String

    /**
     * Returns the label for a bibliography list entry (e.g. `[1]` or empty).
     * Defaults to [getCitationLabel] unless overridden.
     * @param entry the bibliography entry in the list
     * @param index the index of the entry in the bibliography list, starting from 0
     */
    fun getListLabel(
        entry: BibliographyEntry,
        index: Int,
    ): String = getCitationLabel(entry, index)
}
