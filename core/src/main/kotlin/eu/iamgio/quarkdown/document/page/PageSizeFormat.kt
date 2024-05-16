package eu.iamgio.quarkdown.document.page

/**
 * Standard page sizes.
 * @param width width of the page
 * @param height height of the page
 */
enum class PageSizeFormat(val width: Size, val height: Size) {
    A0(841.mm, 1189.mm),
    A1(594.mm, 841.mm),
    A2(420.mm, 594.mm),
    A3(297.mm, 420.mm),
    A4(210.mm, 297.mm),
    A5(148.mm, 210.mm),
    A6(105.mm, 148.mm),
    A7(74.mm, 105.mm),
    A8(52.mm, 74.mm),
    A9(37.mm, 52.mm),
    A10(26.mm, 37.mm),
    B0(1000.mm, 1414.mm),
    B1(707.mm, 1000.mm),
    B2(500.mm, 707.mm),
    B3(353.mm, 500.mm),
    B4(250.mm, 353.mm),
    B5(176.mm, 250.mm),
    LETTER(8.5.inch, 11.0.inch),
    LEGAL(8.5.inch, 14.0.inch),
    LEDGER(11.0.inch, 17.0.inch),
}

/**
 * Represents a size expressed in millimeters.
 */
private val Int.mm: Size
    get() = Size(this.toDouble(), SizeUnit.MM)

/**
 * Represents a size expressed in inches.
 */
private val Double.inch: Size
    get() = Size(this, SizeUnit.IN)
