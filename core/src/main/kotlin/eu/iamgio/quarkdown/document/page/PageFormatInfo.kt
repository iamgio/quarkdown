package eu.iamgio.quarkdown.document.page

/**
 * Information about the format of all pages of a paged document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param pageWidth width of each page
 * @param pageHeight height of each page
 * @param margin blank space around the content of each page
 */
data class PageFormatInfo(
    val pageWidth: Size? = null,
    val pageHeight: Size? = null,
    val margin: Sizes? = null,
) {
    /**
     * Whether the document has a fixed size.
     */
    val hasSize: Boolean
        get() = pageWidth != null && pageHeight != null
}
