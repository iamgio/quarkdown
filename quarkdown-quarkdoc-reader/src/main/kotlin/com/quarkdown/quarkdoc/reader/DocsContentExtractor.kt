package com.quarkdown.quarkdoc.reader

/**
 * Extractor of content of a documentation resource.
 * @see com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlContentExtractor
 */
interface DocsContentExtractor {
    /**
     * @return the extracted main content, if available
     */
    fun extractContent(): String?

    /**
     * @return the function data that this documentation resource describes, if it is about a function
     */
    fun extractFunctionData(): DocsFunction?
}
