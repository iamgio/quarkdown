package com.quarkdown.quarkdoc.reader.json

import java.io.File

/**
 * Recursively discovers [DOCS_INDEX_FILE_NAME] index files within [docsRoot] and reads their content.
 * A documentation tree may carry multiple index files, one per documentation module.
 * @param docsRoot the root directory of the documentation
 * @return the raw JSON content of each discovered index file
 */
fun readDocsIndexes(docsRoot: File): List<String> =
    docsRoot
        .walkTopDown()
        .filter { it.isFile && it.name == DOCS_INDEX_FILE_NAME }
        .map { it.readText() }
        .toList()

/**
 * @param docsRoot the root directory of the documentation
 * @return a walker over all [DOCS_INDEX_FILE_NAME] index files within [docsRoot],
 *         or `null` if it carries none
 * @see readDocsIndexes
 */
fun DocsIndexWalker.Companion.fromDirectoryOrNull(docsRoot: File): DocsIndexWalker? =
    readDocsIndexes(docsRoot)
        .takeIf(List<String>::isNotEmpty)
        ?.let(::DocsIndexWalker)
