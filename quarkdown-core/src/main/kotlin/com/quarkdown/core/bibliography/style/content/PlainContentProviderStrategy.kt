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
 * Content provider for [com.quarkdown.core.bibliography.style.BibliographyStyle.Plain].
 */
internal data object PlainContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    override fun formatAuthor(author: BibliographyEntryAuthor) = author.fullName ?: ""

    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            authors
            ". " then it.title
            ". " then it.journal.emphasized
            ", " then it.volume
            "(" and it.number then ")"
            ":" then it.pages
            ", " then it.year
            ".".just
        }

    override fun visit(entry: BookBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            authors
            ". " then it.title.emphasized
            ". " then it.publisher
            ", " then it.address
            ", " then it.year
            ".".just
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            authors
            ". " then it.title
            genericEntryExtraFields(it)
            ".".just
        }
}
