package com.quarkdown.core.bibliography.style.content

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.style.BibliographyEntryContentProviderStrategy
import com.quarkdown.core.bibliography.style.dsl.buildBibliographyContent
import com.quarkdown.core.bibliography.style.formatAuthors

internal data object ApaContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    override fun formatAuthor(author: BibliographyEntryAuthor): String =
        author.firstName
            ?.take(1)
            ?.let { "${author.lastName}, $it." }
            ?: author.fullName
            ?: "."

    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it).just

            val yearString = it.year?.ifBlank { null } ?: "n.d."
            " (" and yearString then ")"

            ". " then it.title
            ". " then it.journal.emphasized

            if (!it.volume.isNullOrBlank()) {
                ", " then it.volume.emphasized

                if (!it.number.isNullOrBlank()) {
                    "(" and it.number then ")"
                }
            }

            if (!it.pages.isNullOrBlank()) {
                ", " then it.pages
            }

            if (!it.doi.isNullOrBlank()) {
                val cleanDoi = it.doi.replace("^(https?://(dx\\.)?doi\\.org/|doi:)".toRegex(RegexOption.IGNORE_CASE), "")
                (". " and "https://doi.org/$cleanDoi".asLink).just
            } else {
                ".".just
            }
        }

    override fun visit(entry: BookBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it).just

            val yearString = it.year?.ifBlank { null } ?: "n.d."
            " (" and yearString then ")"
            ". " then it.title.emphasized

            val metaInfo = listOfNotNull(
                entry.editor?.ifBlank { null }?.let { e ->
                    val isPlural = e.contains(",") || e.contains("&") || e.contains(" and ")
                    val suffix = if (isPlural) "(Eds.)" else "(Ed.)"

                    "$e $suffix"
                },
                entry.edition?.ifBlank { null },
                entry.volume?.ifBlank { null }?.let { v ->
                    val cleanVolume = v.replace("^V(ol(ume)?)?[.:]?\\s*".toRegex(RegexOption.IGNORE_CASE), "")
                    "Vol. $cleanVolume"
                }
            ).joinToString(separator = ", ")

            if (metaInfo.isNotBlank()) {
                " (" and metaInfo then ")"
            }

            ". " then it.publisher
            ".".just
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it).just

            val yearString = it.year?.ifBlank { null } ?: "n.d."
            " (" and yearString then ")"
            ". " then it.title.emphasized

            if (it.extraFields.isNotEmpty()) {
                val metaData = it.extraFields.map { (key, value) ->
                    "$key: $value"
                }.joinToString(separator = "; ")

                " (" and metaData then ")"
            }

            if (it.url.isNullOrBlank()) {
                ".".just
            } else {
                ". " then it.url.asLink
            }
        }
}