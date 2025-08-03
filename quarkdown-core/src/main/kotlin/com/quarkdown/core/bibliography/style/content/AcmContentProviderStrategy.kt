package com.quarkdown.core.bibliography.style.content

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryContentProviderStrategy
import com.quarkdown.core.bibliography.style.dsl.buildBibliographyContent
import com.quarkdown.core.bibliography.style.formatAuthors
import com.quarkdown.core.bibliography.style.genericEntryExtraFields

/**
 * Content provider for [com.quarkdown.core.bibliography.style.BibliographyStyle.Acm].
 */
internal data object AcmContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    /**
     * @return `Lastname, F.`
     */
    override fun formatAuthor(author: BibliographyEntryAuthor): String =
        author.firstName
            ?.take(1)
            ?.let { "${author.lastName}, $it." }
            ?: author.lastName?.plus(".")
            ?: "."

    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it).smallCaps.just
            " " then it.title
            ". " then it.journal.emphasized
            " " then it.volume.emphasized
            ", " then it.number
            " (" and it.year then ")"
            ", " then it.pages
            ".".just
        }

    override fun visit(entry: BookBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it).smallCaps.just
            " " then it.title.emphasized
            ". " then it.publisher
            ", " then it.address
            ", " then it.year
            ".".just
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it).smallCaps.just
            " " then it.title
            genericEntryExtraFields(it)
            ".".just
        }
}
