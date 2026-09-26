package com.quarkdown.lsp.model

/**
 * Information displayed in a hover tooltip.
 * @param contentMarkdown the tooltip content, in Markdown
 */
data class HoverInfo(
    val contentMarkdown: String,
)
