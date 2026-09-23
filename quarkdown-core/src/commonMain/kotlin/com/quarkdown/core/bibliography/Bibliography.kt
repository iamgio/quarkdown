package com.quarkdown.core.bibliography

/**
 * A document's bibliography.
 * @param entries the bibliography entries associated with their citation keys.
 */
class Bibliography(
    val entries: Map<String, BibliographyEntry>,
) {
    /**
     * @return the index of the given [entry] in the bibliography
     */
    fun indexOf(entry: BibliographyEntry): Int = entries.values.indexOf(entry)
}

/**
 * A single bibliography entry, identified by a unique [citationKey].
 * Formatting and content are handled externally by the [com.quarkdown.core.bibliography.style.BibliographyStyle].
 */
class BibliographyEntry(
    /**
     * The unique identifier for the bibliography entry, used as a citation key.
     */
    val citationKey: String,
)
