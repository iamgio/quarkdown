package com.quarkdown.quarkdoc.reader.json

import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsWalker
import kotlinx.serialization.json.Json

/**
 * Walker of pre-extracted [DocsIndex] payloads, the fast path over
 * [com.quarkdown.quarkdoc.reader.dokka.DokkaHtmlWalker]:
 * content is already in Markdown and requires no HTML processing.
 *
 * A documentation tree may carry multiple [DOCS_INDEX_FILE_NAME] files,
 * one per documentation module, whose entries are merged.
 *
 * @param indexes the raw JSON content of each index file
 * @see fromDirectoryOrNull to discover and read the index files of a local documentation directory
 */
class DocsIndexWalker(
    private val indexes: List<String>,
) : DocsWalker<DocsIndexContentExtractor> {
    override fun walk(): Sequence<DocsWalker.Result<DocsIndexContentExtractor>> =
        indexes
            .asSequence()
            .flatMap { json.decodeFromString<DocsIndex>(it).functions }
            .map { function ->
                DocsWalker.Result(
                    name = function.name,
                    moduleName = function.moduleName,
                    extractor = { DocsIndexContentExtractor(function) },
                )
            }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
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
