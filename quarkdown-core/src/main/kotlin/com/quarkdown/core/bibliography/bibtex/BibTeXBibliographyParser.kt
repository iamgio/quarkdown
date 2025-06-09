package com.quarkdown.core.bibliography.bibtex

import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.BibliographyParser
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry
import org.jbibtex.BibTeXDatabase
import org.jbibtex.BibTeXEntry
import org.jbibtex.BibTeXParser
import java.io.Reader

/**
 * Parser for bibliographies in BibTeX `.bib` format.
 */
object BibTeXBibliographyParser : BibliographyParser {
    override fun parse(reader: Reader): Bibliography {
        val database: BibTeXDatabase = BibTeXParser().parse(reader)
        val entries =
            database.entries.values.map { entry ->
                when (entry.type) {
                    BibTeXEntry.TYPE_ARTICLE -> article(entry)
                    BibTeXEntry.TYPE_BOOK -> book(entry)
                    else -> generic(entry)
                }
            }

        return Bibliography(entries)
    }

    private fun org.jbibtex.Value.toSanitizedString(): String =
        toUserString()
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("~", " ")

    private fun BibTeXEntry.pop(key: org.jbibtex.Key): String? =
        this
            .getField(key)
            ?.toSanitizedString()
            ?.also { removeField(key) }

    private fun BibTeXEntry.extraFields(): Map<String, String> =
        fields
            .mapKeys { it.key.value }
            .mapValues { it.value.toSanitizedString() }

    private fun article(entry: BibTeXEntry): BibliographyEntry =
        with(entry) {
            ArticleBibliographyEntry(
                citationKey = key.value,
                title = pop(BibTeXEntry.KEY_TITLE),
                author = pop(BibTeXEntry.KEY_AUTHOR),
                year = pop(BibTeXEntry.KEY_YEAR),
                journal = pop(BibTeXEntry.KEY_JOURNAL),
                volume = pop(BibTeXEntry.KEY_VOLUME),
                number = pop(BibTeXEntry.KEY_NUMBER),
                pages = pop(BibTeXEntry.KEY_PAGES),
                month = pop(BibTeXEntry.KEY_MONTH),
                doi = pop(BibTeXEntry.KEY_DOI),
                publisher = pop(BibTeXEntry.KEY_PUBLISHER),
                extraFields = extraFields(),
            )
        }

    private fun book(entry: BibTeXEntry): BibliographyEntry =
        with(entry) {
            BookBibliographyEntry(
                citationKey = key.value,
                title = pop(BibTeXEntry.KEY_TITLE),
                author = pop(BibTeXEntry.KEY_AUTHOR),
                year = pop(BibTeXEntry.KEY_YEAR),
                publisher = pop(BibTeXEntry.KEY_PUBLISHER),
                editor = pop(BibTeXEntry.KEY_EDITOR),
                volume = pop(BibTeXEntry.KEY_VOLUME),
                series = pop(BibTeXEntry.KEY_SERIES),
                address = pop(BibTeXEntry.KEY_ADDRESS),
                edition = pop(BibTeXEntry.KEY_EDITION),
                extraFields = extraFields(),
            )
        }

    private fun generic(entry: BibTeXEntry): BibliographyEntry =
        with(entry) {
            GenericBibliographyEntry(
                citationKey = key.value,
                title = pop(BibTeXEntry.KEY_TITLE),
                author = pop(BibTeXEntry.KEY_AUTHOR),
                year = pop(BibTeXEntry.KEY_YEAR),
                extraFields = extraFields(),
            )
        }
}
