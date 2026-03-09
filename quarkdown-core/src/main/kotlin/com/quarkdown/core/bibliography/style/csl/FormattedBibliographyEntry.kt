package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.core.ast.InlineContent

/**
 * A fully formatted bibliography entry produced by the CSL processor.
 * @param label the entry label (e.g. `[1]` for numbered styles, empty for author-year styles)
 * @param content the formatted entry content as Quarkdown AST nodes
 */
internal data class FormattedBibliographyEntry(
    val label: String,
    val content: InlineContent,
)
