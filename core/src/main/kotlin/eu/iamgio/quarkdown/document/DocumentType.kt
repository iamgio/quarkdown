package eu.iamgio.quarkdown.document

import eu.iamgio.quarkdown.document.page.PageOrientation

/**
 * Type of produced document, which affects its post-rendering stage.
 * @param preferredOrientation the preferred orientation of the document, to apply if not overridden by the user
 */
enum class DocumentType(val preferredOrientation: PageOrientation) {
    /**
     * A document whose rendered content is not altered by the post-rendering stage.
     * Plain Markdown is often used as plain (e.g. READMEs).
     */
    PLAIN(PageOrientation.PORTRAIT),

    /**
     * A document that is split into pages of mostly text content: books, articles, papers, etc.
     */
    PAGED(PageOrientation.PORTRAIT),

    /**
     * A slides-based document for presentations.
     */
    SLIDES(PageOrientation.LANDSCAPE),
}
