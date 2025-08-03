package com.quarkdown.core.bibliography

/**
 * Visitor for [BibliographyEntry].
 * @param T return type of the visit operations
 */
interface BibliographyEntryVisitor<T> {
    fun visit(entry: ArticleBibliographyEntry): T

    fun visit(entry: BookBibliographyEntry): T

    fun visit(entry: GenericBibliographyEntry): T
}
