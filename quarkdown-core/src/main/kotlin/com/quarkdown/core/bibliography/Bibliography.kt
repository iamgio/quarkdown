package com.quarkdown.core.bibliography

/**
 * A document's bibliography.
 * @param entries the bibliography entries
 */
data class Bibliography(
    val entries: List<BibliographyEntry>,
)

/**
 * A single bibliography entry.
 * @param citationKey the unique identifier for the entry
 * @param title the title of the work
 * @param author the author(s) of the work
 * @param year the year of publication
 * @param extraFields any additional fields that are not standard
 */
sealed interface BibliographyEntry {
    val citationKey: String
    val title: String?
    val author: String?
    val year: String?
    val extraFields: Map<String, String>
}

/**
 * A bibliography entry for an article (BibTeX type `article`).
 * @param citationKey the unique identifier for the entry
 * @param title the title of the article
 * @param author the author(s) of the article
 * @param year the year of publication
 * @param journal the journal in which the article was published
 * @param volume the volume of the journal
 * @param number the issue number of the journal
 * @param pages the page range of the article
 * @param month the month of publication
 * @param doi the DOI of the article, if available
 * @param publisher the publisher of the journal
 */
data class ArticleBibliographyEntry(
    override val citationKey: String,
    override val title: String?,
    override val author: String?,
    override val year: String?,
    val journal: String?,
    val volume: String?,
    val number: String?,
    val pages: String?,
    val month: String?,
    val doi: String?,
    val publisher: String?,
    override val extraFields: Map<String, String> = emptyMap(),
) : BibliographyEntry

/**
 * A bibliography entry for a book (BibTeX type `book`).
 * @param citationKey the unique identifier for the entry
 * @param title the title of the book
 * @param author the author(s) of the book
 * @param year the year of publication
 * @param publisher the publisher of the book
 * @param editor the editor(s) of the book, if applicable
 * @param volume the volume number, if applicable
 * @param series the series name, if applicable
 * @param address the address of the publisher, if applicable
 * @param edition the edition of the book, if applicable
 */
data class BookBibliographyEntry(
    override val citationKey: String,
    override val title: String?,
    override val author: String?,
    override val year: String?,
    val publisher: String?,
    val editor: String? = null,
    val volume: String? = null,
    val series: String? = null,
    val address: String? = null,
    val edition: String? = null,
    override val extraFields: Map<String, String> = emptyMap(),
) : BibliographyEntry

/**
 * A generic bibliography entry for any other type of work.
 * @param citationKey the unique identifier for the entry
 * @param title the title of the work
 * @param author the author(s) of the work
 * @param year the year of publication
 * @param extraFields any additional fields that are not standard
 */
data class GenericBibliographyEntry(
    override val citationKey: String,
    override val title: String?,
    override val author: String?,
    override val year: String?,
    override val extraFields: Map<String, String>,
) : BibliographyEntry
