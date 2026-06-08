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
     * Walks the documentation files in [docsDirectory] and caches the extracted functions
     * if the cache is not already populated for that directory.
     *
     * Concurrent calls for the same directory only run the walk once: the lock prevents
     * parallel callers from re-walking the docs N times.
     * Empty results are not cached, so a subsequent call will retry.
     *
     * @param docsDirectory the directory containing the documentation files
     */
    fun storeCatalogue(docsDirectory: DocsDirectory) {
        if (catalogue.containsKey(docsDirectory)) return
        synchronized(this) {
            if (catalogue.containsKey(docsDirectory)) return
            val functions = walk(docsDirectory)
            if (functions.isNotEmpty()) catalogue[docsDirectory] = functions
        }
    }

    /**
     * Retrieves the functions from the cache for the given documentation directory.
     * If the cache is empty, it attempts to store the catalogue first.
     * If no functions are found again, an empty sequence is returned.
     * @param docsDirectory the directory containing the documentation files
     * @return a sequence of documented functions
     */
    fun getCatalogue(docsDirectory: DocsDirectory): Sequence<DocumentedFunction> {
        catalogue[docsDirectory]?.let { return it.asSequence() }
        storeCatalogue(docsDirectory)
        return catalogue[docsDirectory]?.asSequence() ?: emptySequence()
    }

    private fun walk(docsDirectory: DocsDirectory): Set<DocumentedFunction> =
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

    /**
     * Searches for functions whose names start with the given query string, case-insensitively.
     * @param docsDirectory the directory containing the documentation files
     * @param nameQuery the query string to search for
     * @return a sequence of documented functions matching the query
     */
    fun searchAll(
        docsDirectory: DocsDirectory,
        nameQuery: String,
    ): Sequence<DocumentedFunction> =
        getCatalogue(docsDirectory)
            .filter { it.data.name.startsWith(nameQuery, ignoreCase = true) }
}
