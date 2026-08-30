package com.quarkdown.quarkdoc.reader

/**
 * Extractor of content of a documentation resource.
 * @see com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlContentExtractor
 */
interface DocsContentExtractor {
    /**
     * @return the extracted main content, if available.
     * The format depends on the source: HTML for Dokka resources,
     * Markdown for pre-extracted index entries
     */
    fun extractContent(): String?

    /**
     * @return the function data that this documentation resource describes, if it is about a function
     */
    fun extractFunctionData(): DocsFunction?
}
