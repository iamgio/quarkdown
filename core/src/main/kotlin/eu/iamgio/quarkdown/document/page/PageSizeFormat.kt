package eu.iamgio.quarkdown.document.page

/**
 * Standard page sizes.
 * @param width width of the page
 * @param height height of the page
 */
enum class PageSizeFormat(val width: Size, val height: Size) {
    A0(841.0.mm, 1189.0.mm),
    A1(594.0.mm, 841.0.mm),
    A2(420.0.mm, 594.0.mm),
    A3(297.0.mm, 420.0.mm),
    A4(210.0.mm, 297.0.mm),
    A5(148.0.mm, 210.0.mm),
    A6(105.0.mm, 148.0.mm),
    A7(74.0.mm, 105.0.mm),
    A8(52.0.mm, 74.0.mm),
    A9(37.0.mm, 52.0.mm),
    A10(26.0.mm, 37.0.mm),
    B0(1000.0.mm, 1414.0.mm),
    B1(707.0.mm, 1000.0.mm),
    B2(500.0.mm, 707.0.mm),
    B3(353.0.mm, 500.0.mm),
    B4(250.0.mm, 353.0.mm),
    B5(176.0.mm, 250.0.mm),
    LETTER(8.5.inch, 11.0.inch),
    LEGAL(8.5.inch, 14.0.inch),
    LEDGER(11.0.inch, 17.0.inch),
}
