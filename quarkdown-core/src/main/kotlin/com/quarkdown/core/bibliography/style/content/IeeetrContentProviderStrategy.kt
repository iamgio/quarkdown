package com.quarkdown.core.bibliography.style.content

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryContentProviderStrategy
import com.quarkdown.core.bibliography.style.dsl.buildBibliographyContent
import com.quarkdown.core.bibliography.style.genericEntryExtraFields

/**
 * Content provider for [com.quarkdown.core.bibliography.style.BibliographyStyle.Ieeetr].
 */
internal data object IeeetrContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    /**
     * @return `F. Lastname`
     */
    override fun formatAuthor(author: BibliographyEntryAuthor): String =
        author.firstName
            ?.take(1)
            ?.let { "$it. ${author.lastName}" }
            ?: author.lastName
            ?: ""

    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            authors
            ", " then it.title.inQuotes
            ", " then it.journal.emphasized
            ", " and "vol. " then it.volume
            ", " and "no. " then it.number
            ", " and "pp. " then it.pages
            ", " then it.year
            ".".just
        }

    override fun visit(entry: BookBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            authors
            ", " then it.title?.emphasized
            ". " then it.address
            ": " then it.publisher
            ", " then it.year
            ".".just
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            authors
            ", " then it.title.inQuotes
            genericEntryExtraFields(it)
            ".".just
        }
}
