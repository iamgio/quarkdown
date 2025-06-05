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
     * @return the parameters of the function that this documentation resource describes, if available
     */
    fun extractFunctionParameters(): List<DocsParameter>
}
