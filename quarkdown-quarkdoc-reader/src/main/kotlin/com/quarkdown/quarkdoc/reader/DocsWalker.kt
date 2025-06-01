package com.quarkdown.quarkdoc.reader

/**
 * A scanner of documentation resources.
 * @param E the type of [DocsContentExtractor] for each scanned resource
 * @see com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
 */
interface DocsWalker<E : DocsContentExtractor> {
    /**
     * Scans documentation resources.
     */
    fun walk(): Sequence<Result<E>>

    /**
     * Represents a scanned documentation resource.
     * @param E the type of [DocsContentExtractor] for the resource
     * @property name the name of the resource (e.g., "lowercase")
     * @property moduleName the name of the Quarkdown module containing the resource (e.g., "String")
     * @property extractor a supplier of a corresponding content extractor that can process the resource
     */
    data class Result<E : DocsContentExtractor>(
        val name: String,
        val moduleName: String,
        val extractor: () -> E,
    )
}
