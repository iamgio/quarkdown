package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.bibliography.ArticleBibliographyEntry
import com.quarkdown.core.bibliography.BookBibliographyEntry
import com.quarkdown.core.bibliography.GenericBibliographyEntry

/**
 * Content provider for [BibliographyStyle.Plain].
 */
internal data object PlainContentProviderStrategy : BibliographyEntryContentProviderStrategy {
    override fun visit(entry: ArticleBibliographyEntry): InlineContent =
        buildInline {
            entry.author?.let {
                text(it)
                text(". ")
            }
            entry.title?.let {
                text(it)
                text(". ")
            }
            entry.journal?.let {
                emphasis { text(it) }
                text(", ")
            }
            entry.volume?.let {
                text(it)
            }
            entry.number?.let {
                text("($it)")
            }
            entry.pages?.let {
                text(":$it")
                text(", ")
            }
            entry.year?.let {
                text(it)
                text(".")
            }
        }

    override fun visit(entry: BookBibliographyEntry): InlineContent =
        buildInline {
            entry.author?.let {
                text(it)
                text(". ")
            }
            entry.title?.let {
                text(it)
                text(". ")
            }
            entry.publisher?.let {
                text(it)
                text(", ")
            }
            entry.address?.let {
                text(it)
                text(", ")
            }
            entry.year?.let {
                text(it)
                text(".")
            }
        }

    override fun visit(entry: GenericBibliographyEntry): InlineContent =
        buildInline {
            entry.author?.let {
                text(it)
                text(". ")
            }
            entry.title?.let {
                text(it)
                text(". ")
            }
            entry.extraFields.forEach { (_, value) ->
                text(value)
                text(". ")
            }
            entry.year?.let {
                text(it)
                text(".")
            }
        }
}
