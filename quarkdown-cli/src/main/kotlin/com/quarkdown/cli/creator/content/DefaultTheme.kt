package com.quarkdown.cli.creator.content

import com.quarkdown.core.document.DocumentType

/**
 * Utilities to determine the default [com.quarkdown.core.document.DocumentTheme] components for a new Quarkdown project,
 * based on its type.
 */
object DefaultTheme {
    private const val DEFAULT_LAYOUT_THEME = "latex"
    private const val DEFAULT_DOCS_LAYOUT_THEME = "hyperlegible"

    private const val DEFAULT_COLOR_THEME = "paperwhite"
    private const val DEFAULT_DOCS_COLOR_THEME = "galactic"

    /**
     * @param type the document type to get the layout theme for
     * @return the default layout theme for the given document type
     */
    fun getLayoutTheme(type: DocumentType): String =
        when (type) {
            DocumentType.DOCS -> DEFAULT_DOCS_LAYOUT_THEME
            else -> DEFAULT_LAYOUT_THEME
        }

    /**
     * @param type the document type to get the color theme for
     * @return the default color theme for the given document type
     */
    fun getColorTheme(type: DocumentType): String =
        when (type) {
            DocumentType.DOCS -> DEFAULT_DOCS_COLOR_THEME
            else -> DEFAULT_COLOR_THEME
        }
}
