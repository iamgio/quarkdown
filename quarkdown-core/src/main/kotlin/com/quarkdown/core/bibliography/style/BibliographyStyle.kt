package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.BibliographyEntry

/**
 * Defines how bibliography entries and citations are formatted.
 * Implementations provide both a [labelProvider] for citation labels and list labels,
 * and a [contentProvider] for the formatted entry content.
 *
 * The primary implementation is [com.quarkdown.core.bibliography.style.csl.CslBibliographyStyle],
 * which supports any [CSL](https://citationstyles.org) style definition.
 */
interface BibliographyStyle {
    /**
     * Strategy to retrieve citation labels for bibliography entries.
     */
    val labelProvider: BibliographyEntryLabelProviderStrategy

    /**
     * Provides the formatted inline content for a bibliography entry.
     * @param entry the bibliography entry to format
     * @return the formatted inline content
     */
    fun contentOf(entry: BibliographyEntry): InlineContent

    /**
     * The name of this bibliography style.
     */
    val name: String
}
