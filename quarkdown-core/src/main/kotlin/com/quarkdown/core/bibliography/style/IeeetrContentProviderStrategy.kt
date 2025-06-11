package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.structuredAuthors

/**
 * Content provider for [BibliographyStyle.Ieeetr].
 */
internal data object IeeetrContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    private fun BibliographyEntry.authorsToString(): String =
        BibliographyStyleUtils.joinAuthorsToString(structuredAuthors) { author ->
            author.firstName
                ?.take(1)
                ?.let { "$it. ${author.lastName}" }
                ?: author.lastName
                ?: ""
        }

    // N. Surname1, N. Surname2, and N. Surname3, “Title of the article,” Journal Name, vol. 1, no. 1, pp. 1–10, 2025.
    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildInline {
            text(entry.authorsToString())

            entry.title?.let {
                text(", ")
                text("“$it”")
            }
            entry.journal?.let {
                text(", ")
                emphasis { text(it) }
            }
            entry.volume?.let {
                text(", ")
                text("vol. $it")
            }
            entry.number?.let {
                text(", ")
                text("no. $it")
            }
            entry.pages?.let {
                text(", ")
                text("pp. $it")
            }
            entry.year?.let {
                text(", ")
                text(it)
            }
            text(".")
        }

    // N. Surname1, N. Surname2, and N. Surname3, Title of the book, Address: Publisher, Year.
    override fun visit(entry: BookBibliographyEntry): InlineContent =
        buildInline {
            text(entry.authorsToString())
            text(", ")

            entry.title?.let {
                emphasis { text(it) }
                text(". ")
            }
            entry.address?.let {
                text(it)
                text(": ")
            }
            entry.publisher?.let {
                text(it)
                text(", ")
            }
            entry.year?.let {
                text(it)
                text(". ")
            }
            text(".")
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildInline {
            text(entry.authorsToString())
            text(", ")

            entry.title?.let {
                text("“$it”")
            }
            BibliographyStyleUtils.run { genericEntryExtraFields(entry) }
            text(".")
        }
}
