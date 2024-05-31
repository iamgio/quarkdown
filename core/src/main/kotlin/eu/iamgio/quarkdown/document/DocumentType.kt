package eu.iamgio.quarkdown.document

/**
 * Type of produced document, which affects its post-rendering stage.
 */
enum class DocumentType {
    /**
     * A document whose rendered content is not altered by the post-rendering stage.
     * Plain Markdown is often used as plain (e.g. READMEs).
     */
    PLAIN,

    /**
     * A document that is split into pages of mostly text content: books, articles, papers, etc.
     */
    PAGED,

    /**
     * A slides-based document for presentations.
     */
    SLIDES,
}
