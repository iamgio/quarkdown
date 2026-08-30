package com.quarkdown.quarkdoc.reader.json

import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsWalker
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Walker of pre-extracted [DocsIndex] files, the fast path over
 * [com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker]:
 * content is already in Markdown and requires no HTML processing.
 *
 * A documentation tree may carry multiple [DOCS_INDEX_FILE_NAME] files,
 * one per documentation module, whose entries are merged.
 */
class DocsIndexWalker(
    private val indexFiles: List<File>,
) : DocsWalker<DocsIndexContentExtractor> {
    override fun walk(): Sequence<DocsWalker.Result<DocsIndexContentExtractor>> =
        indexFiles
            .asSequence()
            .flatMap { json.decodeFromString<DocsIndex>(it.readText()).functions }
            .map { function ->
                DocsWalker.Result(
                    name = function.name,
                    moduleName = function.moduleName,
                    extractor = { DocsIndexContentExtractor(function) },
                )
            }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * @param docsRoot the root directory of the documentation
         * @return a walker over all [DOCS_INDEX_FILE_NAME] index files within [docsRoot],
         *         or `null` if it carries none
         */
        fun fromDirectoryOrNull(docsRoot: File): DocsIndexWalker? =
            docsRoot
                .walkTopDown()
                .filter { it.isFile && it.name == DOCS_INDEX_FILE_NAME }
                .toList()
                .takeIf(List<File>::isNotEmpty)
                ?.let(::DocsIndexWalker)
    }
}

/**
 * Extractor over a pre-extracted [IndexedFunction]: content is returned in Markdown.
 */
class DocsIndexContentExtractor(
    private val function: IndexedFunction,
) : DocsContentExtractor {
    override fun extractContent(): String? = function.contentMarkdown

    override fun extractFunctionData(): DocsFunction = function.function
}
