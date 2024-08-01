package eu.iamgio.quarkdown.document.page

import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.document.size.Sizes

/**
 * Mutable information about the format of all pages of a paged document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param pageWidth width of each page
 * @param pageHeight height of each page
 * @param margin blank space around the content of each page
 */
data class PageFormatInfo(
    var pageWidth: Size? = null,
    var pageHeight: Size? = null,
    var margin: Sizes? = null,
) {
    /**
     * Whether the document has a fixed size.
     */
    val hasSize: Boolean
        get() = pageWidth != null && pageHeight != null
}
