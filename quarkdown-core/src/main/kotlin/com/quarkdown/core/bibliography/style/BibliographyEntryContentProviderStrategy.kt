package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BibliographyEntryVisitor
import com.quarkdown.core.bibliography.structuredAuthors

/**
 * Supplier of rich content for bibliography entries.
 */
interface BibliographyEntryContentProviderStrategy : BibliographyEntryVisitor<InlineContent> {
    /**
     * Formats a single author of a bibliography entry.
     * For instance, some styles may format the author as "F. Lastname", some as "Lastname, F.", or some as "Firstname Lastname".
     * @param author the author to format
     * @return the formatted author string
     */
    fun formatAuthor(author: BibliographyEntryAuthor): String
}

/**
 * Formats the authors of a bibliography entry.
 * This is a convenience method that uses [formatAuthor] to format each author in the entry.
 * @param entry the bibliography entry for which to format the authors
 * @return a string containing all authors formatted according to the strategy
 */
fun BibliographyEntryContentProviderStrategy.formatAuthors(entry: BibliographyEntry): String =
    BibliographyStyleUtils.joinAuthorsToString(entry.structuredAuthors) { author ->
        formatAuthor(author)
    }

/**
 * @param entry the bibliography entry for which to get the content
 * @return the post-processed filtered inline content for the given bibliography entry
 */
fun BibliographyEntryContentProviderStrategy.getContent(entry: BibliographyEntry): InlineContent {
    val content = entry.accept(this)

    // Consecutive punctuation is merged.
    return content.filterIndexed { index, node ->
        val text =
            (node as? Text)
                ?.text
                ?.trim()
                ?: return@filterIndexed true

        val next =
            (content.getOrNull(index + 1) as? Text)
                ?.text
                ?.trim()
                ?: return@filterIndexed true

        fun String.isPunctuation() = this == "," || this == ":" || this == "."

        !(text.isPunctuation() && next.isPunctuation())
    }
}
