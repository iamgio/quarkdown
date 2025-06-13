package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryAuthor
import com.quarkdown.core.bibliography.BibliographyEntryVisitor
import com.quarkdown.core.bibliography.structuredAuthors
import com.quarkdown.core.bibliography.style.BibliographyEntryContentProviderStrategy.Companion.RIGHT_TYPOGRAPHIC_QUOTE

/**
 * Supplier of rich content for bibliography entries.
 */
interface BibliographyEntryContentProviderStrategy : BibliographyEntryVisitor<InlineContent> {
    /**
     * Formats a single author of a bibliography entry.
     * For instance, some styles may format the author as "F. Lastname", some as "Lastname, F.", or some as "Firstname Lastname".
     * @param author the author to format
     * @return the formatted author string
     */
    fun formatAuthor(author: BibliographyEntryAuthor): String

    companion object {
        internal const val LEFT_TYPOGRAPHIC_QUOTE = "“"
        internal const val RIGHT_TYPOGRAPHIC_QUOTE = "”"
    }
}

/**
 * Formats the authors of a bibliography entry.
 * This is a convenience method that uses [formatAuthor] to format each author in the entry.
 * @param entry the bibliography entry for which to format the authors
 * @return a string containing all authors formatted according to the strategy
 */
fun BibliographyEntryContentProviderStrategy.formatAuthors(entry: BibliographyEntry): String =
    BibliographyStyleUtils.joinAuthorsToString(entry.structuredAuthors) { author ->
        formatAuthor(author)
    }

/**
 * @param entry the bibliography entry for which to get the content
 * @return the post-processed filtered inline content for the given bibliography entry
 */
fun BibliographyEntryContentProviderStrategy.getContent(entry: BibliographyEntry): InlineContent {
    val content = entry.accept(this)

    fun String.isPunctuation() = trim() in listOf(".", ",", ";", ":")

    var skipNext = false
    return content.withIndex().mapNotNull { (index, node) ->
        if (skipNext) {
            skipNext = false
            return@mapNotNull null
        }

        val current: String =
            (node as? Text)
                ?.text
                ?: return@mapNotNull node

        val next: String =
            (content.getOrNull(index + 1) as? Text)
                ?.text
                ?: return@mapNotNull node

        // Consecutive punctuation is merged:
        // Text,. -> Text.
        if (current.isPunctuation() && next.isPunctuation()) {
            return@mapNotNull null
        }

        // Punctuation goes before typographic quotes:
        // “Text”. -> “Text.”
        if (current == RIGHT_TYPOGRAPHIC_QUOTE && next.take(1).isPunctuation()) {
            skipNext = true
            return@mapNotNull Text(text = next.take(1) + current + next.drop(1))
        }

        node
    }
}
