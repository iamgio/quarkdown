package eu.iamgio.quarkdown.document

/**
 * Information about the format of all pages of a paged document.
 * @param margin blank space around the content of each page
 */
data class PageFormatInfo(
    val margin: Sizes? = null,
)
