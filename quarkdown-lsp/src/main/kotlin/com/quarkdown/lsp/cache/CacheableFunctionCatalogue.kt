package com.quarkdown.lsp.cache

import com.quarkdown.lsp.documentation.extractContentAsMarkup
import com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private typealias DocsDirectory = File

/**
 * A cache for functions extracted from the Quarkdown documentation.
 * This cache is used to avoid walking and parsing the documentation files,
 * improving performance throughout the language server.
 */
object CacheableFunctionCatalogue {
    private val catalogue = ConcurrentHashMap<DocsDirectory, Set<DocumentedFunction>>()

    /**
     * Stores the functions extracted from the documentation files in the cache,
     * overwriting any existing entries for the given directory.
     * Results are not stored if no functions are found.
     * @param docsDirectory the directory containing the documentation files
     */
    fun storeCatalogue(docsDirectory: DocsDirectory) {
        val functions =
            DokkaHtmlWalker(docsDirectory)
                .walk()
                .filter { it.isInModule }
                .mapNotNull {
                    val extractor = it.extractor()
                    DocumentedFunction(
                        data = extractor.extractFunctionData() ?: return@mapNotNull null,
                        rawData = it,
                        documentationAsMarkup = extractor.extractContentAsMarkup(),
                    )
                }.toSet()

        if (functions.isNotEmpty()) {
            catalogue[docsDirectory] = functions
        }
    }

    /**
     * Retrieves the functions from the cache for the given documentation directory.
     * If the cache is empty, it attempts to store the catalogue first.
     * If no functions are found again, an empty sequence is returned.
     * @param docsDirectory the directory containing the documentation files
     * @return a sequence of documented functions
     */
    fun getCatalogue(docsDirectory: DocsDirectory): Sequence<DocumentedFunction> =
        this.catalogue[docsDirectory]?.asSequence()
            ?: storeCatalogue(docsDirectory).let { this.catalogue[docsDirectory] }?.asSequence()
            ?: emptySequence()
}
