package com.quarkdown.core.bibliography.style

import com.quarkdown.core.bibliography.style.content.AcmContentProviderStrategy
import com.quarkdown.core.bibliography.style.content.IeeetrContentProviderStrategy
import com.quarkdown.core.bibliography.style.content.PlainContentProviderStrategy

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
     * The name of this bibliography style.
     */
    val name: String
        get() = this::class.simpleName?.lowercase() ?: "unknown"

    /**
     * TeX bibliography style `plain`.
     */
    data object Plain : BibliographyStyle {
        override val labelProvider: BibliographyEntryLabelProviderStrategy =
            BibliographyEntryLabelProviderStrategy.IndexOnly
        override val contentProvider: BibliographyEntryContentProviderStrategy =
            PlainContentProviderStrategy
    }

    /**
     * TeX bibliography style `ieeetr`.
     */
    data object Ieeetr : BibliographyStyle {
        override val labelProvider: BibliographyEntryLabelProviderStrategy =
            BibliographyEntryLabelProviderStrategy.IndexOnly
        override val contentProvider: BibliographyEntryContentProviderStrategy =
            IeeetrContentProviderStrategy
    }

    /**
     * TeX bibliography style `acm`.
     */
    data object Acm : BibliographyStyle {
        override val labelProvider: BibliographyEntryLabelProviderStrategy =
            BibliographyEntryLabelProviderStrategy.IndexOnly
        override val contentProvider: BibliographyEntryContentProviderStrategy =
            AcmContentProviderStrategy
    }
}
