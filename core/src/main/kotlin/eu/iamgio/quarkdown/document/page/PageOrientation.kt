package eu.iamgio.quarkdown.document.page

/**
 * The orientation of a page.
 */
enum class PageOrientation {
    /**
     * Vertical orientation, where `height >= width`
     */
    PORTRAIT,

    /**
     * Horizontal orientation, where `width > height`
     */
    LANDSCAPE,
}
