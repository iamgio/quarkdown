package com.quarkdown.core.bibliography.style.content

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import com.quarkdown.core.bibliography.structuredAuthors
import com.quarkdown.core.bibliography.style.BibliographyEntryContentProviderStrategy
import com.quarkdown.core.bibliography.style.dsl.buildBibliographyContent

internal data object ApaContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    override fun formatAuthor(author: BibliographyEntryAuthor): String =
        author.firstName
            ?.let { "${author.lastName}, ${formatInitials(it)}" }
            ?: author.fullName?.let { full ->
                val parts = full.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val last = parts.last()
                    val firstnames = parts.dropLast(1).joinToString(" ")

                    "$last, ${formatInitials(firstnames)}"
                } else {
                    full
                }
            }
            ?: "."

    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it.structuredAuthors).just

            val yearString = it.year?.ifBlank { null } ?: "n.d."
            " (" and yearString then ")"

            ". " then it.title
            ". " then it.journal.emphasized

            if (!it.volume.isNullOrBlank()) {
                ", " then it.volume.emphasized

                if (!it.number.isNullOrBlank()) {
                    "(" and it.number then ")"
                }
            } else if (!it.number.isNullOrBlank()) {
                " (" and it.number then ")"
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
            val hasAuthors = it.structuredAuthors.isNotEmpty()
            val hasEditors = it.structuredEditors.isNotEmpty()

            when {
                hasAuthors -> formatAuthors(it.structuredAuthors).just
                hasEditors -> formatEditors(it.structuredEditors).just
                else -> ".".just
            }

            val yearString = it.year?.takeIf { y -> y.isNotBlank() } ?: "n.d."
            " (" and yearString then ")"
            ". " then it.title.emphasized

            val metaInfo =
                listOfNotNull(
                    if (hasAuthors && hasEditors) formatEditors(it.structuredEditors) else null,
                    entry.edition
                        ?.takeIf { e -> e.isNotBlank() }
                        ?.let { e -> normalizeEdition(e) },
                    entry.volume
                        ?.takeIf { v -> v.isNotBlank() }
                        ?.let { v ->
                            "Vol. ${cleanVolumeNumber(v)}"
                        },
                ).joinToString(separator = ", ")

            if (metaInfo.isNotBlank()) {
                " (" and metaInfo then ")"
            }

            if (!it.publisher.isNullOrBlank()) {
                ". " then it.publisher
            }
            ".".just
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildBibliographyContent(entry) {
            formatAuthors(it.structuredAuthors).just

            val yearString = it.year?.ifBlank { null } ?: "n.d."
            " (" and yearString then ")"
            ". " then it.title.emphasized

            if (it.extraFields.isNotEmpty()) {
                val metaData =
                    it.extraFields
                        .map { (key, value) ->
                            "$key $value"
                        }.joinToString(separator = "; ")

                " (" and metaData then ")"
            }

            if (it.url.isNullOrBlank()) {
                ".".just
            } else {
                ". " then it.url.asLink
            }
        }

    private fun formatAuthors(authors: List<BibliographyEntryAuthor>): String =
        joinAuthorsToString(
            authors = authors,
            lastSeparator = ", & ",
        ) { author -> formatAuthor(author) }

    private fun formatEditors(editors: List<BibliographyEntryAuthor>): String {
        val formatted = formatAuthors(editors)
        val suffix = if (editors.size > 1) " (Eds.)" else " (Ed.)"
        return formatted + suffix
    }

    private fun formatInitials(names: String): String =
        names
            .replace("\\s*-\\s*".toRegex(), "-")
            .replace("\\s+".toRegex(), " ")
            .replace("(\\p{L})[^\\s-]*".toRegex(), "$1.")
            .trim()

    private fun joinAuthorsToString(
        authors: List<BibliographyEntryAuthor>,
        separator: String = ", ",
        lastSeparator: String = ", & ",
        transform: (BibliographyEntryAuthor) -> String,
    ): String =
        buildString {
            if (authors.size <= 20) {
                authors.forEachIndexed { index, author ->
                    append(transform(author))
                    if (index < authors.size - 2) {
                        append(separator)
                    } else if (index == authors.size - 2) {
                        append(lastSeparator)
                    }
                }
            } else {
                authors.take(19).forEach { author ->
                    append(transform(author)).append(separator)
                }

                append("... ").append(transform(authors.last()))
            }
        }

    private fun normalizeEdition(raw: String): String {
        val number = raw.filter { it.isDigit() }
        return if (number.isNotBlank()) {
            "$number${ordinalSuffix(number.toInt())} ed."
        } else {
            raw
        }
    }

    private fun ordinalSuffix(n: Int): String =
        if (n % 100 in 11..13) {
            "th"
        } else {
            when (n % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }

    private fun cleanVolumeNumber(raw: String): String = raw.replace("^V(ol(ume)?)?[.:]?\\s*".toRegex(RegexOption.IGNORE_CASE), "")
}
