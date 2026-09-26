package com.quarkdown.lsp.documentation

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.quarkdoc.reader.json.DOCS_INDEX_FILE_NAME

/**
 * Source of pre-extracted documentation index payloads ([DOCS_INDEX_FILE_NAME]),
 * abstracting away how the indexes are located and read:
 * from the installation's docs directory on the JVM,
 * or fetched over the network in browser deployments.
 *
 * Sources are used as cache keys by [com.quarkdown.lsp.cache.CacheableFunctionCatalogue],
 * so implementations should provide value equality.
 */
fun interface DocsIndexSource {
    /**
     * @return the raw JSON content of each available documentation index
     */
    fun readIndexes(): List<String>
}

/**
 * A [DocsIndexSource] that recursively discovers index files within a [directory], on any file system.
 * @param directory the root directory of the documentation
 */
data class DirectoryDocsIndexSource(
    private val directory: FsEntry,
) : DocsIndexSource {
    override fun readIndexes(): List<String> =
        directory
            .descendants()
            .filter { it.isFile && it.name == DOCS_INDEX_FILE_NAME }
            .map { it.readText() }
            .toList()
}
