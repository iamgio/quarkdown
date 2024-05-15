package eu.iamgio.quarkdown.rendering.wrapper

/**
 * Placeholders for a [RenderWrapper] template.
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
     * Whether the document uses a theme.
     */
    const val HAS_THEME = "THEME"

    /**
     * Whether block codes are used and highlighting-related scripts should be loaded.
     */
    const val HAS_CODE = "CODE"

    /**
     * Whether math is used and math-related scripts should be loaded.
     */
    const val HAS_MATH = "MATH"

    /**
     * Whether this document is grouped in pages.
     */
    const val IS_PAGED = "PAGED"

    /**
     * Size of the document.
     */
    const val PAGE_SIZE = "PAGESIZE"

    /**
     * Margin of each page.
     */
    const val PAGE_MARGIN = "PAGEMARGIN"
}
