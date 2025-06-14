package com.quarkdown.core.bibliography.style

import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.style.dsl.BibliographyEntryContentBuilder

/**
 * Utilities for generating bibliography styles.
 * @see BibliographyStyle
 */
internal object BibliographyStyleUtils {
    /**
     * Joins a list of authors into a string.
     * @param authors the list of authors to join
     * @param separator the separator to use between authors
     * @param lastSeparator the separator to use before the last author
     * @param transform a function to transform each author into a string
     * @return a string representation of the authors
     */
    fun joinAuthorsToString(
        authors: List<BibliographyEntryAuthor>,
        separator: String = ", ",
        lastSeparator: String = ", and ",
        transform: (BibliographyEntryAuthor) -> String,
    ): String =
        buildString {
            authors.forEachIndexed { index, author ->
                append(transform(author))
                if (index < authors.size - 2) {
                    append(separator)
                } else if (index == authors.size - 2) {
                    append(lastSeparator)
                }
            }
        }
}

/**
 * Appends extra fields of a generic bibliography entry to the inline content.
 * @param entry the generic bibliography entry
 */
internal fun BibliographyEntryContentBuilder<GenericBibliographyEntry>.genericEntryExtraFields(entry: GenericBibliographyEntry) {
    entry.extraFields.forEach { (_, value) ->
        ". " then value
    }
    ". " then entry.year
    ". " then entry.url.asLink
}
