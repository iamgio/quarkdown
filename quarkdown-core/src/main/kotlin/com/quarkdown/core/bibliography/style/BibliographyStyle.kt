package com.quarkdown.core.bibliography.style

/**
 * TeX-inspired styles for rendering bibliography entries.
 */
interface BibliographyStyle {
    /**
     * Strategy to retrieve citation labels for bibliography entries.
     */
    val labelProvider: BibliographyEntryLabelProviderStrategy

    /**
     * Strategy to retrieve content for bibliography entries.
     */
    val contentProvider: BibliographyEntryContentProviderStrategy

    /**
     * TeX bibliography style `plain`.
     */
    data object Plain : BibliographyStyle {
        override val labelProvider: BibliographyEntryLabelProviderStrategy =
            BibliographyEntryLabelProviderStrategy.IndexOnly
        override val contentProvider: BibliographyEntryContentProviderStrategy =
            PlainContentProviderStrategy
    }
}
