package eu.iamgio.quarkdown.document

import eu.iamgio.quarkdown.document.numbering.DocumentNumbering
import eu.iamgio.quarkdown.document.numbering.NumberingFormat
import eu.iamgio.quarkdown.document.page.PageOrientation

/**
 * Type of produced document, which affects its post-rendering stage.
 * @param preferredOrientation the preferred orientation of the document, to apply if not overridden by the user
 * @param defaultNumbering the default numbering formats to apply, if not overridden by the user
 */
enum class DocumentType(
    val preferredOrientation: PageOrientation,
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
        DocumentNumbering(
            headings = NumberingFormat.fromString("1.1.1"),
            figures = NumberingFormat.fromString("1.1"),
            tables = NumberingFormat.fromString("1.1"),
        ),
    ),

    /**
     * A slides-based document for presentations.
     */
    SLIDES(PageOrientation.LANDSCAPE),
}
