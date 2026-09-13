package com.quarkdown.lsp.cache

import com.quarkdown.lsp.documentation.DocsIndexSource
import com.quarkdown.quarkdoc.reader.json.DocsIndexWalker
import java.util.concurrent.ConcurrentHashMap

/**
 * A cache for functions extracted from the Quarkdown documentation index.
 * This cache is used to avoid re-reading and reparsing the index,
 * improving performance throughout the language server.
 */
object CacheableFunctionCatalogue {
    private val catalogue = ConcurrentHashMap<DocsIndexSource, Set<DocumentedFunction>>()

    /**
     * Reads the documentation index from [docs] and caches the extracted functions
     * if the cache is not already populated for that source.
     *
     * Concurrent calls for the same source only run the extraction once: the lock prevents
     * parallel callers from re-reading the index N times.
     * Empty results are not cached, so a subsequent call will retry.
     *
     * @param docs the source of the documentation index
     */
    fun storeCatalogue(docs: DocsIndexSource) {
        if (catalogue.containsKey(docs)) return
        synchronized(this) {
            if (catalogue.containsKey(docs)) return
            val functions = walk(docs)
            if (functions.isNotEmpty()) catalogue[docs] = functions
        }
    }

    /**
     * Retrieves the functions from the cache for the given documentation source.
     * If the cache is empty, it attempts to store the catalogue first.
     * If no functions are found again, an empty sequence is returned.
     * @param docs the source of the documentation index
     * @return a sequence of documented functions
     */
    fun getCatalogue(docs: DocsIndexSource): Sequence<DocumentedFunction> {
        catalogue[docs]?.let { return it.asSequence() }
        storeCatalogue(docs)
        return catalogue[docs]?.asSequence() ?: emptySequence()
    }

    /**
     * Loads the documented functions from the pre-extracted documentation index,
     * which carries descriptions and documentation in Markdown.
     * @return the documented functions, or an empty set if the source carries no index
     */
    private fun walk(docs: DocsIndexSource): Set<DocumentedFunction> =
        DocsIndexWalker(docs.readIndexes())
            .walk()
            .filter { it.isInModule }
            .map {
                val extractor = it.extractor()
                val data = extractor.extractFunctionData()
                DocumentedFunction(
                    data = data,
                    rawData = it,
                    documentationMarkdown = extractor.extractContent(),
                )
            }.toSet()

    /**
     * Searches for functions whose names start with the given query string, case-insensitively.
     * @param docs the source of the documentation index
     * @param nameQuery the query string to search for
     * @return a sequence of documented functions matching the query
     */
    fun searchAll(
        docs: DocsIndexSource,
        nameQuery: String,
    ): Sequence<DocumentedFunction> =
        getCatalogue(docs)
            .filter { it.data.name.startsWith(nameQuery, ignoreCase = true) }
}
