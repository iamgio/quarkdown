package com.quarkdown.core.bibliography.style

import com.quarkdown.core.bibliography.BibliographyEntry

private const val LABEL_BEGIN = "["
private const val LABEL_END = "]"

/**
 * Supplier of citation labels for bibliography entries.
 */
interface BibliographyEntryLabelProviderStrategy {
    /**
     * @param entry the bibliography entry for which to get the label
     * @param index the index of the entry in the bibliography list, starting from 0
     * @returns the citation label for the given bibliography entry.
     */
    fun getLabel(
        entry: BibliographyEntry,
        index: Int,
    ): String

    /**
     * [BibliographyEntryLabelProviderStrategy] that provides labels in the format `[1]`, `[2]`, etc.
     */
    data object IndexOnly : BibliographyEntryLabelProviderStrategy {
        override fun getLabel(
            entry: BibliographyEntry,
            index: Int,
        ): String = LABEL_BEGIN + (index + 1) + LABEL_END
    }
}
