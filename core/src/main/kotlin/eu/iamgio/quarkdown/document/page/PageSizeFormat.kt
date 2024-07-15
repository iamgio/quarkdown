package eu.iamgio.quarkdown.document.page

/**
 * Standard page sizes.
 * @param bounds size the page
 */
enum class PageSizeFormat(private val bounds: BoundingBox) {
    A0(841.0.mm by 1189.0.mm),
    A1(594.0.mm by 841.0.mm),
    A2(420.0.mm by 594.0.mm),
    A3(297.0.mm by 420.0.mm),
    A4(210.0.mm by 297.0.mm),
    A5(148.0.mm by 210.0.mm),
    A6(105.0.mm by 148.0.mm),
    A7(74.0.mm by 105.0.mm),
    A8(52.0.mm by 74.0.mm),
    A9(37.0.mm by 52.0.mm),
    A10(26.0.mm by 37.0.mm),
    B0(1000.0.mm by 1414.0.mm),
    B1(707.0.mm by 1000.0.mm),
    B2(500.0.mm by 707.0.mm),
    B3(353.0.mm by 500.0.mm),
    B4(250.0.mm by 353.0.mm),
    B5(176.0.mm by 250.0.mm),
    LETTER(8.5.inch by 11.0.inch),
    LEGAL(8.5.inch by 14.0.inch),
    LEDGER(11.0.inch by 17.0.inch),
    ;

    /**
     * Base orientation of the format.
     */
    private val orientation: PageOrientation
        // Assuming width and height are declared with the same size unit.
        get() = if (bounds.width.value > bounds.height.value) PageOrientation.LANDSCAPE else PageOrientation.PORTRAIT

    /**
     * @param orientation orientation of the page
     * @return the bounds of the format for the given orientation
     * If, for instance, the document is landscape and the given format is portrait, the format is converted to landscape.
     */
    fun getBounds(orientation: PageOrientation): BoundingBox = if (this.orientation == orientation) bounds else bounds.rotated
}
