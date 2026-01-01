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
     * Path to the root of the document from the current location.
     */
    const val ROOT_PATH = "ROOTPATH"

    /**
     * Title of the document.
     */
    const val TITLE = "TITLE"

    /**
     * Authors of the document.
     */
    const val AUTHORS = "AUTHORS"

    /**
     * Description of the document.
     */
    const val DESCRIPTION = "DESCRIPTION"

    /**
     * Keywords of the document.
     */
    const val KEYWORDS = "KEYWORDS"

    /**
     * Language of the document.
     */
    const val LANGUAGE = "LANG"

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
     * Whether this document is plain, with no pages.
     * @see com.quarkdown.core.document.DocumentType.PLAIN
     */
    const val IS_PLAIN = "PLAIN"

    /**
     * Whether this document is grouped in pages.
     * @see com.quarkdown.core.document.DocumentType.PAGED
     */
    const val IS_PAGED = "PAGED"

    /**
     * Whether this document is a presentation.
     * @see com.quarkdown.core.document.DocumentType.SLIDES
     */
    const val IS_SLIDES = "SLIDES"

    /**
     * Whether this document is docs.
     * @see com.quarkdown.core.document.DocumentType.DOCS
     */
    const val IS_DOCS = "DOCS"

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
     * Width of the border around the content area of each page.
     */
    const val PAGE_CONTENT_BORDER_WIDTH = "PAGEBORDERWIDTH"

    /**
     * Color of the border around the content area of each page.
     */
    const val PAGE_CONTENT_BORDER_COLOR = "PAGEBORDERCOLOR"

    /**
     * Font size of the text on each page.
     */
    const val FONT_SIZE = "FONTSIZE"

    /**
     * Number of columns on each page.
     */
    const val COLUMN_COUNT = "COLUMNCOUNT"

    /**
     * Horizontal content alignment of each page.
     * This is applied globally to all elements in the document.
     * For instance: start, center, end.
     */
    const val GLOBAL_HORIZONTAL_ALIGNMENT = "HALIGNMENT_GLOBAL"

    /**
     * Horizontal content alignment of each page.
     * This is applied locally to selected elements in the document.
     * For instance: justify.
     */
    const val LOCAL_HORIZONTAL_ALIGNMENT = "HALIGNMENT_LOCAL"

    /**
     * Line height of paragraphs.
     */
    const val PARAGRAPH_LINE_HEIGHT = "PARAGRAPHLINEHEIGHT"

    /**
     * Whitespace width between words.
     */
    const val PARAGRAPH_LETTER_SPACING = "PARAGRAPHLETTERSPACING"

    /**
     * Whitespace height between paragraphs.
     */
    const val PARAGRAPH_SPACING = "PARAGRAPHSPACING"

    /**
     * Indentation width of the first line of paragraphs.
     */
    const val PARAGRAPH_INDENT = "PARAGRAPHINDENT"

    /**
     * Font faces to load.
     */
    const val FONT_FACES = "FONTFACE"

    /**
     * Main font family to use in the document.
     */
    const val MAIN_FONT_FAMILY = "MAINFONTFAMILY"

    /**
     * Font family to use in the document to use for headings.
     */
    const val HEADING_FONT_FAMILY = "HEADINGFONTFAMILY"

    /**
     * Font family to use in the document to use for code blocks and code spans.
     */
    const val CODE_FONT_FAMILY = "CODEFONTFAMILY"

    /**
     * Custom user-defined TeX macros.
     */
    const val TEX_MACROS = "TEXMACRO"
}
