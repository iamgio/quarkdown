package com.quarkdown.rendering.html.pdf

/**
 * Conversion of DOM measures into `Page.printToPDF` paper sizes.
 */
internal object PdfPaperSize {
    /**
     * CSS reference pixel density, used by Chromium to convert pixel-based sizes to inches.
     */
    private const val CSS_PIXELS_PER_INCH = 96.0

    /**
     * Additional height added to single-page PDFs. If not enough, an extra page would be incorrectly generated.
     */
    private const val SINGLE_PAGE_HEIGHT_PADDING_PX = 100.0

    /**
     * Multiplier applied to the body height of single-page PDFs, for the same reason as [SINGLE_PAGE_HEIGHT_PADDING_PX].
     */
    private const val SINGLE_PAGE_HEIGHT_MULTIPLIER = 1.03

    /**
     * Computes the paper height, in inches, of a single-page (plain document type) PDF.
     * @param bodyClientHeightPx the `clientHeight` of the document body, in CSS pixels
     * @return the paper height to pass to `Page.printToPDF`
     */
    fun singlePageHeightInches(bodyClientHeightPx: Double): Double =
        (bodyClientHeightPx * SINGLE_PAGE_HEIGHT_MULTIPLIER + SINGLE_PAGE_HEIGHT_PADDING_PX) / CSS_PIXELS_PER_INCH
}
