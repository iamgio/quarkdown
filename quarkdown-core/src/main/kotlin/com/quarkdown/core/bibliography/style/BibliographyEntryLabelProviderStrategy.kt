package com.quarkdown.core.bibliography.style

import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.structuredAuthors

private const val LABEL_BEGIN = "["
private const val LABEL_END = "]"

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

    /**
     * Provides labels in the format `[1]`, `[2]`, etc., for both citations and list entries.
     */
    data object IndexOnly : BibliographyEntryLabelProviderStrategy {
        override fun getCitationLabel(
            entry: BibliographyEntry,
            index: Int,
        ): String = LABEL_BEGIN + (index + 1) + LABEL_END
    }

    /**
     * Provides author-year citation labels in the format `(Author, Year)`,
     * following APA style conventions:
     * - 1 author: `(Einstein, 1905)`
     * - 2 authors: `(Einstein & Bohr, 1935)`
     * - 3+ authors: `(Einstein et al., 1935)`
     * - No author: first words of the title are used as fallback.
     * - No year: `n.d.` (no date) is used.
     *
     * List labels are empty, as APA bibliography lists use hanging indents instead of labels.
     */
    data object AuthorYear : BibliographyEntryLabelProviderStrategy {
        override fun getCitationLabel(
            entry: BibliographyEntry,
            index: Int,
        ): String {
            val authors = entry.structuredAuthors
            val authorPart =
                when (authors.size) {
                    0 ->
                        entry.title
                            ?.split(" ")
                            ?.take(3)
                            ?.joinToString(" ") ?: "?"
                    1 -> authors[0].lastName ?: authors[0].fullName ?: "?"
                    2 -> "${authors[0].lastName ?: "?"} & ${authors[1].lastName ?: "?"}"
                    else -> "${authors[0].lastName ?: "?"} et al."
                }
            val year = entry.year?.ifBlank { null } ?: "n.d."
            return "($authorPart, $year)"
        }

        override fun getListLabel(
            entry: BibliographyEntry,
            index: Int,
        ): String = ""
    }
}
