package com.quarkdown.core.bibliography.style

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.BibliographyEntryVisitor

/**
 * Supplier of rich content for bibliography entries.
 */
interface BibliographyEntryContentProviderStrategy : BibliographyEntryVisitor<InlineContent>

/**
 * @param entry the bibliography entry for which to get the content
 * @return the post-processed filtered inline content for the given bibliography entry
 */
fun BibliographyEntryContentProviderStrategy.getContent(entry: BibliographyEntry): InlineContent {
    val content = entry.accept(this)

    // Consecutive punctuation is merged.
    return content.filterIndexed { index, node ->
        val text =
            (node as? Text)
                ?.text
                ?.trim()
                ?: return@filterIndexed true

        val next =
            (content.getOrNull(index + 1) as? Text)
                ?.text
                ?.trim()
                ?: return@filterIndexed true

        fun String.isPunctuation() = this == "," || this == ":" || this == "."

        !(text.isPunctuation() && next.isPunctuation())
    }
}
