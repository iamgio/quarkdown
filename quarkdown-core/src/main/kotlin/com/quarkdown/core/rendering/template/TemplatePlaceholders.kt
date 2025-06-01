package com.quarkdown.core.rendering.template

import com.quarkdown.core.template.TemplateProcessor

/**
 * Placeholders for a [TemplateProcessor] used for the post-rendering stage,
 * which involves wrapping the output of the rendering stage in a template.
 */
object TemplatePlaceholders {
    /**
     * Content of the rendering stage.
     */
    const val CONTENT = "CONTENT"

    /**
     * Title of the document.
     */
    const val TITLE = "TITLE"

    /**
     * Language of the document.
     */
    const val LANGUAGE = "LANG"

    /**
     * Port of the local server port to communicate with.
     */
    const val SERVER_PORT = "SERVERPORT"

    /**
     * Whether block codes are used and highlighting-related scripts should be loaded.
     */
    const val HAS_CODE = "CODE"

    /**
     * Whether Mermaid diagrams are used and diagram-related scripts should be loaded.
     */
    const val HAS_MERMAID_DIAGRAM = "MERMAID"

    /**
     * Whether math is used and math-related scripts should be loaded.
     */
    const val HAS_MATH = "MATH"

    /**
     * Document type, lowercase.
     */
    const val DOCUMENT_TYPE = "DOCTYPE"

    /**
     * Whether this document is grouped in pages.
     */
    const val IS_PAGED = "PAGED"

    /**
     * Whether this document is a presentation.
     */
    const val IS_SLIDES = "SLIDES"

    /**
     * Whether the document has a fixed size.
     */
    const val HAS_PAGE_SIZE = "PAGESIZE"

    /**
     * Width of the document.
     */
    const val PAGE_WIDTH = "PAGEWIDTH"

    /**
     * Height of the document.
     */
    const val PAGE_HEIGHT = "PAGEHEIGHT"

    /**
     * Margin of each page.
     */
    const val PAGE_MARGIN = "PAGEMARGIN"

    /**
     * Number of columns on each page.
     */
    const val COLUMN_COUNT = "COLUMNCOUNT"

    /**
     * Horizontal content alignment of each page.
     */
    const val HORIZONTAL_ALIGNMENT = "HALIGNMENT"

    /**
     * Line height of paragraphs.
     */
    const val PARAGRAPH_LINE_HEIGHT = "PARAGRAPHLINEHEIGHT"

    /**
     * Whitespace height between paragraphs.
     */
    const val PARAGRAPH_SPACING = "PARAGRAPHSPACING"

    /**
     * Indentation width of the first line of paragraphs.
     */
    const val PARAGRAPH_INDENT = "PARAGRAPHINDENT"

    /**
     * Custom user-defined TeX macros.
     */
    const val TEX_MACROS = "TEXMACRO"
}
