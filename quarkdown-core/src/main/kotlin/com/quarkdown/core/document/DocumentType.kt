package com.quarkdown.core.document

import com.quarkdown.core.document.layout.page.PageFormatInfo
import com.quarkdown.core.document.layout.page.PageOrientation
import com.quarkdown.core.document.layout.page.PageSizeFormat
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.document.numbering.NumberingFormat

/**
 * Type of produced document, which affects its post-rendering stage.
 * @param preferredOrientation the preferred orientation of the document, to apply if not overridden by the user
 * @param defaultPageFormat the default page format to apply, if not overridden by the user
 * @param defaultNumbering the default numbering formats to apply, if not overridden by the user
 */
enum class DocumentType(
    val preferredOrientation: PageOrientation,
    val defaultPageFormat: PageFormatInfo? = null,
    val defaultNumbering: DocumentNumbering? = null,
) {
    /**
     * A document whose rendered content is not altered by the post-rendering stage.
     * Plain Markdown is often used as plain (e.g. READMEs).
     */
    PLAIN(PageOrientation.PORTRAIT),

    /**
     * A document that is split into pages of mostly text content: books, articles, papers, etc.
     */
    PAGED(
        PageOrientation.PORTRAIT,
        defaultPageFormat =
            with(PageSizeFormat.A4.getBounds(PageOrientation.PORTRAIT)) {
                PageFormatInfo(
                    pageWidth = width,
                    pageHeight = height,
                )
            },
        defaultNumbering =
            DocumentNumbering(
                headings = NumberingFormat.fromString("1.1.1"),
                figures = NumberingFormat.fromString("1.1"),
                tables = NumberingFormat.fromString("1.1"),
                math = NumberingFormat.fromString("(1)"),
            ),
    ),

    /**
     * A slides-based document for presentations.
     */
    SLIDES(PageOrientation.LANDSCAPE),
}
